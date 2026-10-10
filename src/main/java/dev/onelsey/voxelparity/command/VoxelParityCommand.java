package dev.onelsey.voxelparity.command;

import dev.onelsey.voxelparity.VoxelParityPlugin;
import dev.onelsey.voxelparity.bedrock.BedrockPlayerDetector;
import dev.onelsey.voxelparity.collision.paper.CollisionMetrics;
import dev.onelsey.voxelparity.collision.paper.CollisionParityService;
import dev.onelsey.voxelparity.collision.paper.TranslatedCollisionKind;
import dev.onelsey.voxelparity.debug.DebugRegistry;
import dev.onelsey.voxelparity.debug.MovementTraceRecorder;
import dev.onelsey.voxelparity.module.ModuleManager;
import dev.onelsey.voxelparity.movement.MovementModule;
import dev.onelsey.voxelparity.spectator.SpectatorAssistService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VoxelParityCommand implements CommandExecutor, TabCompleter {
    private final VoxelParityPlugin plugin;
    private final BedrockPlayerDetector detector;
    private final DebugRegistry debugRegistry;
    private final ModuleManager moduleManager;
    private final MovementTraceRecorder traceRecorder;
    private final SpectatorAssistService spectatorAssist;

    public VoxelParityCommand(
            VoxelParityPlugin plugin,
            BedrockPlayerDetector detector,
            DebugRegistry debugRegistry,
            ModuleManager moduleManager,
            MovementTraceRecorder traceRecorder,
            SpectatorAssistService spectatorAssist
    ) {
        this.plugin = plugin;
        this.detector = detector;
        this.debugRegistry = debugRegistry;
        this.moduleManager = moduleManager;
        this.traceRecorder = traceRecorder;
        this.spectatorAssist = spectatorAssist;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission("voxelparity.admin")) {
            sender.sendMessage("You do not have permission to use VoxelParity administration commands.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "status" -> sendStatus(sender);
            case "modules" -> sendModules(sender);
            case "reload" -> reload(sender);
            case "debug" -> debug(sender, args);
            case "dump" -> dump(sender, args);
            default -> sendHelp(sender, label);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission("voxelparity.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return filter(args[0], List.of("status", "modules", "debug", "dump", "reload"));
        }

        if (args.length == 2
                && (args[0].equalsIgnoreCase("debug")
                || args[0].equalsIgnoreCase("dump"))) {
            Set<String> names = new LinkedHashSet<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            names.addAll(traceRecorder.recentPlayerNames());
            return filter(args[1], new ArrayList<>(names));
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("debug")) {
            return filter(args[2], List.of("on", "off"));
        }

        return List.of();
    }

    private void sendStatus(CommandSender sender) {
        int activeModules = moduleManager.enabledModuleCount()
                + (spectatorAssist.enabled() ? 1 : 0);
        sender.sendMessage("VoxelParity " + plugin.getPluginMeta().getVersion());
        sender.sendMessage("Bedrock detector: " + detector.providerName());
        sender.sendMessage("Enabled modules: " + activeModules);
        sender.sendMessage("Global debug logging: " + debugRegistry.logAll());
        sender.sendMessage(
                "Recorder: enabled=" + traceRecorder.enabled()
                        + ", trackedPlayers=" + traceRecorder.trackedPlayers()
                        + ", entriesPerPlayer=" + traceRecorder.entriesPerPlayer()
        );
    }

    private void sendModules(CommandSender sender) {
        for (MovementModule module : moduleManager.modules()) {
            sender.sendMessage(
                    module.id()
                            + ": enabled=" + module.enabled()
                            + ", allowed=" + module.allowedMoves()
                            + ", blocked=" + module.blockedCandidates()
            );
        }

        CollisionParityService collision = moduleManager.collisionParityService();
        for (TranslatedCollisionKind kind : TranslatedCollisionKind.values()) {
            CollisionMetrics.Snapshot metrics = collision.metrics(kind);
            sender.sendMessage(
                    kind.id()
                            + ": enabled=" + collision.enabled(kind)
                            + ", allowed=" + metrics.allowedFailed()
                            + ", corrected=" + metrics.correctedAccepted()
                            + ", poseAccepted=" + metrics.poseAccepted()
                            + ", poseFailedAllowed=" + metrics.poseFailedAllowed()
                            + ", blocked=" + metrics.blockedFailed()
            );
        }

        sender.sendMessage(
                "spectator-assist: enabled=" + spectatorAssist.enabled()
                        + ", bursts=" + spectatorAssist.assistBursts()
                        + ", steps=" + spectatorAssist.assistedSteps()
                        + ", activeTasks=" + spectatorAssist.activeTasks()
        );
    }

    private void reload(CommandSender sender) {
        plugin.reloadVoxelParityConfiguration();
        sender.sendMessage("VoxelParity configuration reloaded.");
    }

    private void debug(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /vp debug <player> [on|off]");
            return;
        }

        Player player = Bukkit.getPlayerExact(args[1]);
        if (player == null) {
            sender.sendMessage("Player not found: " + args[1]);
            return;
        }

        boolean enabled;
        if (args.length >= 3) {
            if (args[2].equalsIgnoreCase("on")) {
                enabled = true;
            } else if (args[2].equalsIgnoreCase("off")) {
                enabled = false;
            } else {
                sender.sendMessage("Usage: /vp debug <player> [on|off]");
                return;
            }
        } else {
            enabled = !debugRegistry.isPlayerEnabled(player.getUniqueId());
        }

        debugRegistry.setPlayerEnabled(player.getUniqueId(), enabled);
        sender.sendMessage("Debug for " + player.getName() + ": " + enabled);
    }

    private void dump(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /vp dump <player>");
            return;
        }

        Optional<MovementTraceRecorder.DumpTicket> ticket =
                traceRecorder.dumpAsync(args[1]);
        if (ticket.isEmpty()) {
            sender.sendMessage("No recent VoxelParity trace data for: " + args[1]);
            return;
        }

        MovementTraceRecorder.DumpTicket value = ticket.get();
        sender.sendMessage(
                "Queued movement dump for " + value.playerName()
                        + " (" + value.entries() + " entries): "
                        + value.relativePath()
        );
    }

    private static void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("/" + label + " status");
        sender.sendMessage("/" + label + " modules");
        sender.sendMessage("/" + label + " debug <player> [on|off]");
        sender.sendMessage("/" + label + " dump <player>");
        sender.sendMessage("/" + label + " reload");
    }

    private static List<String> filter(String input, List<String> values) {
        String prefix = input.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();
    }
}
