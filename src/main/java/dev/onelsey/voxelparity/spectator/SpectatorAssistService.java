package dev.onelsey.voxelparity.spectator;

import dev.onelsey.voxelparity.bedrock.BedrockPlayerDetector;
import dev.onelsey.voxelparity.debug.MovementTraceRecorder;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public final class SpectatorAssistService implements Listener {
    private static final long INPUT_TRACE_INTERVAL_NANOS = 100_000_000L;

    private final JavaPlugin plugin;
    private final BedrockPlayerDetector detector;
    private final MovementTraceRecorder recorder;
    private final ConcurrentHashMap<UUID, State> states = new ConcurrentHashMap<>();
    private final Set<UUID> activeTasks = ConcurrentHashMap.newKeySet();
    private final LongAdder assistBursts = new LongAdder();
    private final LongAdder assistedSteps = new LongAdder();

    private volatile boolean enabled = true;
    private volatile int stallTicks = 2;
    private volatile int burstTicks = 5;
    private volatile int idleTaskTicks = 20;
    private volatile double minimumProgress = 0.015D;
    private volatile double step = 0.22D;
    private volatile double sprintMultiplier = 1.35D;

    public SpectatorAssistService(
            JavaPlugin plugin,
            BedrockPlayerDetector detector,
            MovementTraceRecorder recorder
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.detector = Objects.requireNonNull(detector, "detector");
        this.recorder = Objects.requireNonNull(recorder, "recorder");
    }

    public void reload(FileConfiguration config) {
        enabled = config.getBoolean("modules.spectator-assist.enabled", true);
        stallTicks = clamp(
                config.getInt("modules.spectator-assist.stall-ticks", 2),
                1,
                10
        );
        burstTicks = clamp(
                config.getInt("modules.spectator-assist.burst-ticks", 5),
                1,
                20
        );
        idleTaskTicks = clamp(
                config.getInt("modules.spectator-assist.idle-task-ticks", 20),
                5,
                200
        );
        minimumProgress = clamp(
                config.getDouble("modules.spectator-assist.minimum-progress", 0.015D),
                0.001D,
                0.25D
        );
        step = clamp(
                config.getDouble("modules.spectator-assist.step", 0.22D),
                0.05D,
                0.75D
        );
        sprintMultiplier = clamp(
                config.getDouble("modules.spectator-assist.sprint-multiplier", 1.35D),
                1.0D,
                2.5D
        );
    }

    public boolean enabled() {
        return enabled;
    }

    public long assistBursts() {
        return assistBursts.sum();
    }

    public long assistedSteps() {
        return assistedSteps.sum();
    }

    public int activeTasks() {
        return activeTasks.size();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInput(PlayerInputEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!detector.isBedrockPlayer(uuid)) {
            return;
        }

        State state = states.computeIfAbsent(uuid, ignored -> new State());
        state.input = SpectatorInputState.from(event.getInput());
        state.cancelRequested = false;

        if (!enabled || player.getGameMode() != GameMode.SPECTATOR) {
            state.cancelRequested = true;
            return;
        }

        recorder.recordRateLimited(
                player,
                "SPECTATOR_INPUT",
                INPUT_TRACE_INTERVAL_NANOS,
                state.input.compact() + " pos=" + format(player.getLocation())
        );

        ensureTask(player, state);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!detector.isBedrockPlayer(uuid)) {
            return;
        }

        if (event.getNewGameMode() == GameMode.SPECTATOR && enabled) {
            State state = states.computeIfAbsent(uuid, ignored -> new State());
            state.reset();
            recorder.record(
                    player,
                    "SPECTATOR",
                    "enter from=" + player.getGameMode()
                            + " pos=" + format(player.getLocation())
            );
            return;
        }

        State state = states.get(uuid);
        if (state != null) {
            state.cancelRequested = true;
        }
        recorder.record(
                player,
                "SPECTATOR",
                "exit to=" + event.getNewGameMode()
                        + " pos=" + format(player.getLocation())
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        State state = states.get(uuid);
        if (state != null) {
            state.cancelRequested = true;
        }
    }

    private void ensureTask(Player player, State state) {
        UUID uuid = player.getUniqueId();
        if (!activeTasks.add(uuid)) {
            return;
        }

        ScheduledTask scheduled = player.getScheduler().runAtFixedRate(
                plugin,
                task -> tick(player, state, task),
                () -> cleanup(uuid),
                1L,
                1L
        );
        if (scheduled == null) {
            cleanup(uuid);
        }
    }

    private void tick(Player player, State state, ScheduledTask task) {
        UUID uuid = player.getUniqueId();
        if (state.cancelRequested
                || !enabled
                || player.getGameMode() != GameMode.SPECTATOR
                || !detector.isBedrockPlayer(uuid)) {
            task.cancel();
            cleanup(uuid);
            return;
        }

        SpectatorInputState input = state.input;
        Location current = player.getLocation();

        if (!input.hasMovement()) {
            state.idleTicks++;
            state.stalledTicks = 0;
            state.burstRemaining = 0;
            state.lastPosition = Position.from(current);

            if (state.wasAssisting) {
                player.setVelocity(new Vector(0.0D, 0.0D, 0.0D));
                state.wasAssisting = false;
            }

            if (state.idleTicks >= idleTaskTicks) {
                task.cancel();
                activeTasks.remove(uuid);
            }
            return;
        }

        state.idleTicks = 0;
        SpectatorAssistVector.Direction direction =
                SpectatorAssistVector.direction(input, current.getYaw(), current.getPitch());
        if (direction.isZero()) {
            state.lastPosition = Position.from(current);
            return;
        }

        Position previous = state.lastPosition;
        if (previous == null || !previous.worldId.equals(current.getWorld().getUID())) {
            state.lastPosition = Position.from(current);
            return;
        }

        double progress = previous.progressToward(current, direction);
        if (state.burstRemaining <= 0) {
            if (progress < minimumProgress) {
                state.stalledTicks++;
            } else {
                state.stalledTicks = 0;
            }

            if (state.stalledTicks < stallTicks) {
                state.lastPosition = Position.from(current);
                return;
            }

            state.stalledTicks = 0;
            state.burstRemaining = burstTicks;
            assistBursts.increment();
            recorder.record(
                    player,
                    "SPECTATOR_ASSIST",
                    "burst-start input={" + input.compact() + "}"
                            + " progress=" + String.format(Locale.ROOT, "%.5f", progress)
                            + " direction=" + direction
                            + " pos=" + format(current)
            );
        }

        if (state.teleportPending) {
            return;
        }

        double distance = step * (input.sprint() ? sprintMultiplier : 1.0D);
        Location target = current.clone().add(
                direction.x() * distance,
                direction.y() * distance,
                direction.z() * distance
        );
        World world = target.getWorld();
        if (world == null || !world.isChunkLoaded(target.getBlockX() >> 4, target.getBlockZ() >> 4)) {
            state.burstRemaining = 0;
            state.lastPosition = Position.from(current);
            recorder.record(
                    player,
                    "SPECTATOR_ASSIST",
                    "skip-unloaded-target pos=" + format(target)
            );
            return;
        }

        state.teleportPending = true;
        state.burstRemaining--;
        state.wasAssisting = true;
        state.lastPosition = Position.from(target);
        player.setVelocity(new Vector(0.0D, 0.0D, 0.0D));

        player.teleportAsync(target, PlayerTeleportEvent.TeleportCause.PLUGIN)
                .whenComplete((success, failure) -> {
                    state.teleportPending = false;
                    if (failure == null && Boolean.TRUE.equals(success)) {
                        assistedSteps.increment();
                    }
                });
    }

    private void cleanup(UUID uuid) {
        activeTasks.remove(uuid);
        states.remove(uuid);
    }

    private static String format(Location location) {
        return String.format(
                Locale.ROOT,
                "%.4f,%.4f,%.4f",
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class State {
        private volatile SpectatorInputState input = SpectatorInputState.idle();
        private volatile boolean cancelRequested;
        private volatile boolean teleportPending;
        private Position lastPosition;
        private int stalledTicks;
        private int burstRemaining;
        private int idleTicks;
        private boolean wasAssisting;

        private void reset() {
            input = SpectatorInputState.idle();
            cancelRequested = false;
            teleportPending = false;
            lastPosition = null;
            stalledTicks = 0;
            burstRemaining = 0;
            idleTicks = 0;
            wasAssisting = false;
        }
    }

    private record Position(UUID worldId, double x, double y, double z) {
        private static Position from(Location location) {
            return new Position(
                    location.getWorld().getUID(),
                    location.getX(),
                    location.getY(),
                    location.getZ()
            );
        }

        private double progressToward(
                Location current,
                SpectatorAssistVector.Direction direction
        ) {
            return (current.getX() - x) * direction.x()
                    + (current.getY() - y) * direction.y()
                    + (current.getZ() - z) * direction.z();
        }
    }
}
