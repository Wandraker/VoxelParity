package dev.onelsey.voxelparity.movement;

import dev.onelsey.voxelparity.bedrock.BedrockPlayerDetector;
import dev.onelsey.voxelparity.debug.DebugRegistry;
import dev.onelsey.voxelparity.module.ModuleManager;
import dev.onelsey.voxelparity.util.BoxMath;
import io.papermc.paper.event.player.PlayerFailMoveEvent;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;

public final class MovementListener implements Listener {
    private final JavaPlugin plugin;
    private final BedrockPlayerDetector detector;
    private final DebugRegistry debugRegistry;
    private final ModuleManager moduleManager;

    public MovementListener(
            JavaPlugin plugin,
            BedrockPlayerDetector detector,
            DebugRegistry debugRegistry,
            ModuleManager moduleManager
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.detector = Objects.requireNonNull(detector, "detector");
        this.debugRegistry = Objects.requireNonNull(debugRegistry, "debugRegistry");
        this.moduleManager = Objects.requireNonNull(moduleManager, "moduleManager");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFailMove(PlayerFailMoveEvent event) {
        if (event.isAllowed() || event.getFailReason() != PlayerFailMoveEvent.FailReason.CLIPPED_INTO_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (!detector.isBedrockPlayer(player.getUniqueId())) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        World fromWorld = from.getWorld();
        if (fromWorld == null || !fromWorld.equals(to.getWorld())) {
            return;
        }

        BoundingBox fromBox = BoxMath.playerBoxAt(player, from);
        BoundingBox toBox = BoxMath.playerBoxAt(player, to);
        FailMoveContext context = new FailMoveContext(
                player,
                from,
                to,
                fromBox,
                toBox,
                BoxMath.union(fromBox, toBox)
        );

        for (MovementModule module : moduleManager.modules()) {
            if (!module.enabled()) {
                continue;
            }

            ModuleEvaluation evaluation = module.evaluate(context);
            if (debugRegistry.isEnabled(player.getUniqueId())) {
                plugin.getLogger().info(
                        "[debug] player=" + player.getName()
                                + " module=" + evaluation.moduleId()
                                + " outcome=" + evaluation.outcome()
                                + " reason=" + evaluation.reason()
                                + " from=" + format(from)
                                + " to=" + format(to)
                );
            }

            if (evaluation.outcome() == ModuleEvaluation.Outcome.ALLOW) {
                event.setAllowed(true);
                if (evaluation.suppressVanillaWarning()) {
                    event.setLogWarning(false);
                }
                return;
            }
        }
    }

    private static String format(Location location) {
        return String.format("%.4f,%.4f,%.4f", location.getX(), location.getY(), location.getZ());
    }
}
