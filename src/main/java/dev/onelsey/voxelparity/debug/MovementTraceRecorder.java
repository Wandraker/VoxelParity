package dev.onelsey.voxelparity.debug;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MovementTraceRecorder {
    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT)
                    .withZone(ZoneOffset.UTC);
    private static final int MAX_MESSAGE_LENGTH = 2048;

    private final JavaPlugin plugin;
    private final ConcurrentHashMap<UUID, PlayerTrace> traces = new ConcurrentHashMap<>();

    private volatile boolean enabled = true;
    private volatile int entriesPerPlayer = 128;
    private volatile int maxPlayers = 64;

    public MovementTraceRecorder(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload(FileConfiguration config) {
        enabled = config.getBoolean("recorder.enabled", true);
        entriesPerPlayer = clamp(config.getInt("recorder.entries-per-player", 128), 32, 512);
        maxPlayers = clamp(config.getInt("recorder.max-players", 64), 8, 256);

        for (PlayerTrace trace : traces.values()) {
            trace.buffer.resize(entriesPerPlayer);
        }
        trimPlayers();
    }

    public boolean enabled() {
        return enabled;
    }

    public int trackedPlayers() {
        return traces.size();
    }

    public int entriesPerPlayer() {
        return entriesPerPlayer;
    }

    public void record(Player player, String category, String message) {
        if (!enabled) {
            return;
        }
        traceFor(player).record(category, sanitize(message), 0L);
    }

    public void recordRateLimited(
            Player player,
            String category,
            long minimumIntervalNanos,
            String message
    ) {
        if (!enabled) {
            return;
        }
        traceFor(player).record(
                category,
                sanitize(message),
                Math.max(0L, minimumIntervalNanos)
        );
    }

    public Optional<DumpTicket> dumpAsync(String playerName) {
        PlayerTrace trace = findByName(playerName);
        if (trace == null) {
            return Optional.empty();
        }

        List<TraceRingBuffer.TraceEntry> entries = trace.buffer.snapshot();
        if (entries.isEmpty()) {
            return Optional.empty();
        }

        String safeName = trace.name.replaceAll("[^A-Za-z0-9_.-]", "_");
        String fileName = FILE_TIME.format(Instant.now()) + "-" + safeName + ".log";
        Path directory = plugin.getDataFolder().toPath().resolve("dumps");
        Path file = directory.resolve(fileName);
        String content = render(trace, entries);

        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                Files.createDirectories(directory);
                Files.writeString(
                        file,
                        content,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE
                );
                plugin.getLogger().info(
                        "Movement trace dump written: " + file.toAbsolutePath()
                );
            } catch (IOException exception) {
                plugin.getLogger().severe(
                        "Failed to write movement trace dump " + file + ": "
                                + exception.getMessage()
                );
            }
        });

        return Optional.of(new DumpTicket(
                Path.of("plugins", plugin.getName(), "dumps", fileName),
                entries.size(),
                trace.name
        ));
    }

    public List<String> recentPlayerNames() {
        return traces.values().stream()
                .sorted(Comparator.comparingLong(PlayerTrace::lastTouchedNanos).reversed())
                .map(PlayerTrace::name)
                .distinct()
                .toList();
    }

    private PlayerTrace traceFor(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerTrace trace = traces.get(uuid);
        if (trace != null) {
            trace.name = player.getName();
            trace.touch();
            return trace;
        }

        if (traces.size() >= maxPlayers) {
            evictOldest();
        }

        PlayerTrace created = new PlayerTrace(
                uuid,
                player.getName(),
                new TraceRingBuffer(entriesPerPlayer)
        );
        PlayerTrace existing = traces.putIfAbsent(uuid, created);
        return existing == null ? created : existing;
    }

    private PlayerTrace findByName(String playerName) {
        for (PlayerTrace trace : traces.values()) {
            if (trace.name.equalsIgnoreCase(playerName)) {
                return trace;
            }
        }
        return null;
    }

    private void trimPlayers() {
        while (traces.size() > maxPlayers) {
            evictOldest();
        }
    }

    private void evictOldest() {
        traces.values().stream()
                .min(Comparator.comparingLong(PlayerTrace::lastTouchedNanos))
                .ifPresent(trace -> traces.remove(trace.uuid, trace));
    }

    private String render(
            PlayerTrace trace,
            List<TraceRingBuffer.TraceEntry> entries
    ) {
        List<String> lines = new ArrayList<>();
        lines.add("VoxelParity " + plugin.getPluginMeta().getVersion());
        lines.add("player=" + trace.name);
        lines.add("uuid=" + trace.uuid);
        lines.add("generated=" + Instant.now());
        lines.add("entries=" + entries.size());
        lines.add("");

        for (TraceRingBuffer.TraceEntry entry : entries) {
            lines.add(
                    Instant.ofEpochMilli(entry.epochMillis())
                            + " [" + entry.category() + "] "
                            + entry.message()
            );
        }

        return String.join(System.lineSeparator(), lines)
                + System.lineSeparator();
    }

    private static String sanitize(String message) {
        String singleLine = message.replace('\n', ' ').replace('\r', ' ').trim();
        if (singleLine.length() <= MAX_MESSAGE_LENGTH) {
            return singleLine;
        }
        return singleLine.substring(0, MAX_MESSAGE_LENGTH) + "...";
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class PlayerTrace {
        private final UUID uuid;
        private final TraceRingBuffer buffer;
        private final Map<String, Long> lastCategoryNanos = new HashMap<>();
        private volatile String name;
        private volatile long lastTouchedNanos = System.nanoTime();

        private PlayerTrace(UUID uuid, String name, TraceRingBuffer buffer) {
            this.uuid = uuid;
            this.name = name;
            this.buffer = buffer;
        }

        private synchronized void record(
                String category,
                String message,
                long minimumIntervalNanos
        ) {
            long now = System.nanoTime();
            if (minimumIntervalNanos > 0L) {
                Long previous = lastCategoryNanos.get(category);
                if (previous != null && now - previous < minimumIntervalNanos) {
                    return;
                }
                lastCategoryNanos.put(category, now);
            }

            buffer.add(new TraceRingBuffer.TraceEntry(
                    System.currentTimeMillis(),
                    category,
                    message
            ));
            lastTouchedNanos = now;
        }

        private void touch() {
            lastTouchedNanos = System.nanoTime();
        }

        private long lastTouchedNanos() {
            return lastTouchedNanos;
        }

        private String name() {
            return name;
        }
    }

    public record DumpTicket(Path relativePath, int entries, String playerName) {
    }
}
