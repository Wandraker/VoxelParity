package dev.onelsey.voxelparity.movement;

import dev.onelsey.voxelparity.bedrock.BedrockPlayerDetector;
import dev.onelsey.voxelparity.collision.math.Aabb;
import dev.onelsey.voxelparity.collision.paper.CollisionParityService;
import dev.onelsey.voxelparity.collision.paper.PaperBoxes;
import dev.onelsey.voxelparity.debug.DebugRegistry;
import dev.onelsey.voxelparity.module.ModuleManager;
import io.papermc.paper.event.player.PlayerFailMoveEvent;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
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
        if (!sameWorld(from, to)) {
            return;
        }

        BoundingBox fromBox = dev.onelsey.voxelparity.util.BoxMath.playerBoxAt(player, from);
        BoundingBox toBox = dev.onelsey.voxelparity.util.BoxMath.playerBoxAt(player, to);
        FailMoveContext context = new FailMoveContext(
                player,
                from,
                to,
                fromBox,
                toBox,
                dev.onelsey.voxelparity.util.BoxMath.union(fromBox, toBox)
        );

        for (MovementModule module : moduleManager.modules()) {
            if (!module.enabled()) {
                continue;
            }

            ModuleEvaluation evaluation = module.evaluate(context);
            logDecision(player, evaluation.moduleId(), evaluation.outcome().name(), evaluation.reason(), from, to);
            if (evaluation.outcome() == ModuleEvaluation.Outcome.ALLOW) {
                event.setAllowed(true);
                if (evaluation.suppressVanillaWarning()) {
                    event.setLogWarning(false);
                }
                return;
            }
        }

        Aabb target = PaperBoxes.playerAt(player, to);
        CollisionParityService.FailedMoveDecision collisionDecision =
                moduleManager.collisionParityService().evaluateFailedMove(player, target);
        logDecision(
                player,
                "translated-collision",
                collisionDecision.outcome().name(),
                collisionDecision.reason(),
                from,
                to
        );
        if (collisionDecision.outcome() == CollisionParityService.FailedMoveDecision.Outcome.ALLOW) {
            event.setAllowed(true);
            event.setLogWarning(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event instanceof PlayerTeleportEvent
                || !event.hasChangedPosition()
                || !sameWorld(event.getFrom(), event.getTo())) {
            return;
        }

        Player player = event.getPlayer();
        if (!detector.isBedrockPlayer(player.getUniqueId())) {
            return;
        }

        Optional<Location> corrected = moduleManager.collisionParityService().correctAcceptedMove(
                player,
                event.getFrom(),
                event.getTo()
        );
        corrected.ifPresent(location -> {
            if (debugRegistry.isEnabled(player.getUniqueId())) {
                plugin.getLogger().info(
                        "[debug] player=" + player.getName()
                                + " module=translated-collision outcome=CORRECT"
                                + " from=" + format(event.getFrom())
                                + " requested=" + format(event.getTo())
                                + " corrected=" + format(location)
                );
            }
            event.setTo(location);
        });
    }

    private void logDecision(
            Player player,
            String module,
            String outcome,
            String reason,
            Location from,
            Location to
    ) {
        if (!debugRegistry.isEnabled(player.getUniqueId())) {
            return;
        }
        plugin.getLogger().info(
                "[debug] player=" + player.getName()
                        + " module=" + module
                        + " outcome=" + outcome
                        + " reason=" + reason
                        + " from=" + format(from)
                        + " to=" + format(to)
        );
    }

    private static boolean sameWorld(Location from, Location to) {
        World world = from.getWorld();
        return world != null && world.equals(to.getWorld());
    }

    private static String format(Location location) {
        return String.format("%.4f,%.4f,%.4f", location.getX(), location.getY(), location.getZ());
    }
}
