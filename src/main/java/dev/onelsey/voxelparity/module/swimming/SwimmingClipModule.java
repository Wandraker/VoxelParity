package dev.onelsey.voxelparity.module.swimming;

import dev.onelsey.voxelparity.collision.math.Aabb;
import dev.onelsey.voxelparity.collision.paper.PaperBoxes;
import dev.onelsey.voxelparity.movement.FailMoveContext;
import dev.onelsey.voxelparity.movement.ModuleEvaluation;
import dev.onelsey.voxelparity.movement.MovementModule;
import dev.onelsey.voxelparity.util.WorldCollisionScanner;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Pose;
import org.bukkit.util.BoundingBox;

public final class SwimmingClipModule implements MovementModule {
    private static final String ID = "swimming-collision";
    private static final double SWIMMING_HEIGHT = 0.6D;

    private final LongAdder allowedMoves = new LongAdder();
    private final LongAdder blockedCandidates = new LongAdder();

    private volatile boolean enabled;
    private volatile double maxMovementPerAxis;
    private volatile boolean suppressVanillaWarning;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public void reload(FileConfiguration config) {
        enabled = config.getBoolean("modules.swimming-collision.enabled", true);
        maxMovementPerAxis = Math.max(
                0.1D,
                config.getDouble("modules.swimming-collision.max-movement-per-axis", 1.0D)
        );
        suppressVanillaWarning = config.getBoolean(
                "modules.swimming-collision.suppress-vanilla-warning",
                true
        );
    }

    @Override
    public ModuleEvaluation evaluate(FailMoveContext context) {
        boolean swimmingPose = context.player().getPose() == Pose.SWIMMING;
        boolean swimmingIntent = context.player().isSwimming();
        if (!swimmingPose && !swimmingIntent) {
            return ModuleEvaluation.abstain(ID, "player-not-swimming");
        }

        double dx = context.to().getX() - context.from().getX();
        double dy = context.to().getY() - context.from().getY();
        double dz = context.to().getZ() - context.from().getZ();
        if (Math.abs(dx) > maxMovementPerAxis
                || Math.abs(dy) > maxMovementPerAxis
                || Math.abs(dz) > maxMovementPerAxis) {
            return ModuleEvaluation.abstain(ID, "movement-exceeds-module-limit");
        }

        Aabb start = PaperBoxes.fromPaper(context.fromBox());
        Aabb target = PaperBoxes.fromPaper(context.toBox());
        if (!swimmingPose && swimmingIntent) {
            start = SwimmingPathGuard.compactAtFeet(start, SWIMMING_HEIGHT);
            target = SwimmingPathGuard.compactAtFeet(target, SWIMMING_HEIGHT);
        }

        BoundingBox swept = PaperBoxes.toPaper(start.union(target));
        WorldCollisionScanner.DetailedScan scan = WorldCollisionScanner.scanDetailed(
                context.player().getWorld(),
                swept,
                Material.POWDER_SNOW
        );

        if (scan.scanLimitExceeded()) {
            blockedCandidates.increment();
            return ModuleEvaluation.blocked(ID, "scan-limit-exceeded");
        }
        if (scan.unloadedChunkEncountered()) {
            blockedCandidates.increment();
            return ModuleEvaluation.blocked(ID, "unloaded-chunk-in-swept-volume");
        }
        if (scan.specialBlockEncountered()) {
            return ModuleEvaluation.abstain(ID, "powder-snow-in-swept-volume");
        }

        List<Aabb> obstacles = scan.collisionBoxes().stream()
                .map(PaperBoxes::fromPaper)
                .toList();

        if (obstacles.stream().anyMatch(target::overlaps)) {
            return ModuleEvaluation.abstain(ID, "java-target-collision");
        }
        if (!SwimmingPathGuard.canAllow(start, target, obstacles)) {
            return ModuleEvaluation.abstain(ID, "java-path-collision");
        }

        allowedMoves.increment();
        String reason = swimmingPose
                ? "swimming-path-clear"
                : "swimming-intent-compact-path-clear";
        return ModuleEvaluation.allow(
                ID,
                reason,
                suppressVanillaWarning
        );
    }

    @Override
    public long allowedMoves() {
        return allowedMoves.sum();
    }

    @Override
    public long blockedCandidates() {
        return blockedCandidates.sum();
    }
}
