package dev.onelsey.voxelparity.movement;

import dev.onelsey.voxelparity.bedrock.BedrockPlayerDetector;
import dev.onelsey.voxelparity.collision.math.Aabb;
import dev.onelsey.voxelparity.collision.paper.CollisionParityService;
import dev.onelsey.voxelparity.collision.paper.CollisionWorld;
import dev.onelsey.voxelparity.collision.paper.PaperBoxes;
import dev.onelsey.voxelparity.debug.DebugRegistry;
import dev.onelsey.voxelparity.module.ModuleManager;
import io.papermc.paper.event.player.PlayerFailMoveEvent;
import java.util.List;
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

        Aabb fromAabb = PaperBoxes.fromPaper(fromBox);
        Aabb target = PaperBoxes.fromPaper(toBox);
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

        if (debugRegistry.isEnabled(player.getUniqueId())
                && collisionDecision.outcome() != CollisionParityService.FailedMoveDecision.Outcome.ABSTAIN) {
            logCollisionDiagnostics(
                    player,
                    "FAIL_MOVE_" + collisionDecision.outcome().name(),
                    fromAabb,
                    target,
                    fromAabb.union(target),
                    moduleManager.collisionParityService().diagnostics(
                            player,
                            fromAabb.union(target),
                            target
                    )
            );
        }

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

        Aabb start = PaperBoxes.playerAt(player, event.getFrom());
        Aabb requestedTarget = PaperBoxes.playerAt(player, event.getTo());

        Optional<Location> corrected = moduleManager.collisionParityService().correctAcceptedMove(
                player,
                event.getFrom(),
                event.getTo()
        );
        corrected.ifPresent(location -> {
            if (debugRegistry.isEnabled(player.getUniqueId())) {
                Aabb correctedTarget = PaperBoxes.playerAt(player, location);
                plugin.getLogger().info(
                        "[debug] player=" + player.getName()
                                + " module=translated-collision outcome=CORRECT"
                                + " from=" + format(event.getFrom())
                                + " requested=" + format(event.getTo())
                                + " corrected=" + format(location)
                );
                logCollisionDiagnostics(
                        player,
                        "MOVE_CORRECT",
                        start,
                        requestedTarget,
                        start.union(requestedTarget),
                        moduleManager.collisionParityService().diagnostics(
                                player,
                                start.union(requestedTarget),
                                requestedTarget
                        )
                );
                plugin.getLogger().info(
                        "[debug-geometry] player=" + player.getName()
                                + " phase=MOVE_CORRECT correctedTarget=" + format(correctedTarget)
                );
            }
            event.setTo(location);
        });
    }

    private void logCollisionDiagnostics(
            Player player,
            String phase,
            Aabb start,
            Aabb target,
            Aabb swept,
            List<CollisionWorld.TranslatedCollisionDiagnostic> diagnostics
    ) {
        plugin.getLogger().info(
                "[debug-geometry] player=" + player.getName()
                        + " phase=" + phase
                        + " start=" + format(start)
                        + " target=" + format(target)
                        + " swept=" + format(swept)
                        + " translatedBlocks=" + diagnostics.size()
        );

        for (CollisionWorld.TranslatedCollisionDiagnostic diagnostic : diagnostics) {
            plugin.getLogger().info(
                    "[debug-geometry] player=" + player.getName()
                            + " phase=" + phase
                            + " block=" + diagnostic.material()
                            + " pos=" + diagnostic.x() + "," + diagnostic.y() + "," + diagnostic.z()
                            + " data=" + diagnostic.blockData()
                            + " javaBoxes=" + formatBoxes(diagnostic.javaBoxes())
                            + " bedrockBox=" + format(diagnostic.bedrockBox())
                            + " javaTargetOverlap=" + diagnostic.javaTargetOverlap()
                            + " bedrockTargetOverlap=" + diagnostic.bedrockTargetOverlap()
            );
        }
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

    private static String format(Aabb box) {
        return String.format(
                "[%.6f,%.6f,%.6f -> %.6f,%.6f,%.6f]",
                box.minX(),
                box.minY(),
                box.minZ(),
                box.maxX(),
                box.maxY(),
                box.maxZ()
        );
    }

    private static String formatBoxes(List<Aabb> boxes) {
        if (boxes.isEmpty()) {
            return "[]";
        }
        return boxes.stream()
                .map(MovementListener::format)
                .toList()
                .toString();
    }
}
