package dev.onelsey.voxelparity.command;

import dev.onelsey.voxelparity.VoxelParityPlugin;
import dev.onelsey.voxelparity.bedrock.BedrockPlayerDetector;
import dev.onelsey.voxelparity.collision.paper.CollisionParityService;
import dev.onelsey.voxelparity.debug.DebugRegistry;
import dev.onelsey.voxelparity.module.ModuleManager;
import dev.onelsey.voxelparity.movement.MovementModule;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    public VoxelParityCommand(
            VoxelParityPlugin plugin,
            BedrockPlayerDetector detector,
            DebugRegistry debugRegistry,
            ModuleManager moduleManager
    ) {
        this.plugin = plugin;
        this.detector = detector;
        this.debugRegistry = debugRegistry;
        this.moduleManager = moduleManager;
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
            return filter(args[0], List.of("status", "modules", "debug", "reload"));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return filter(args[1], names);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("debug")) {
            return filter(args[2], List.of("on", "off"));
        }

        return List.of();
    }

    private void sendStatus(CommandSender sender) {
        sender.sendMessage("VoxelParity " + plugin.getPluginMeta().getVersion());
        sender.sendMessage("Bedrock detector: " + detector.providerName());
        sender.sendMessage("Enabled modules: " + moduleManager.enabledModuleCount());
        sender.sendMessage("Global debug logging: " + debugRegistry.logAll());
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
        sender.sendMessage(
                "bamboo: enabled=" + collision.bambooEnabled()
                        + ", sharedAllowed=" + collision.allowedFailedMoves()
                        + ", sharedCorrected=" + collision.correctedAcceptedMoves()
                        + ", sharedBlocked=" + collision.blockedFailedCandidates()
        );
        sender.sendMessage(
                "pointed-dripstone: enabled=" + collision.pointedDripstoneEnabled()
                        + ", sharedAllowed=" + collision.allowedFailedMoves()
                        + ", sharedCorrected=" + collision.correctedAcceptedMoves()
                        + ", sharedBlocked=" + collision.blockedFailedCandidates()
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

    private static void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("/" + label + " status");
        sender.sendMessage("/" + label + " modules");
        sender.sendMessage("/" + label + " debug <player> [on|off]");
        sender.sendMessage("/" + label + " reload");
    }

    private static List<String> filter(String input, List<String> values) {
        String prefix = input.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();
    }
}
