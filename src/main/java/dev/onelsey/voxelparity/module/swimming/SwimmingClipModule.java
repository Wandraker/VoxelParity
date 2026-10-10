package dev.onelsey.voxelparity.module.swimming;

import dev.onelsey.voxelparity.movement.FailMoveContext;
import dev.onelsey.voxelparity.movement.ModuleEvaluation;
import dev.onelsey.voxelparity.movement.MovementModule;
import dev.onelsey.voxelparity.util.WorldCollisionScanner;
import java.util.concurrent.atomic.LongAdder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Pose;

public final class SwimmingClipModule implements MovementModule {
    private static final String ID = "swimming-collision";

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
        if (context.player().getPose() != Pose.SWIMMING) {
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

        WorldCollisionScanner.ScanResult scan = WorldCollisionScanner.scan(
                context.player().getWorld(),
                context.sweptBox(),
                Material.POWDER_SNOW
        );

        if (!SwimmingClipGuard.canAllow(
                scan.unloadedChunkEncountered(),
                scan.specialBlockEncountered(),
                scan.otherCollisionEncountered()
        )) {
            if (scan.unloadedChunkEncountered()) {
                blockedCandidates.increment();
                return ModuleEvaluation.blocked(ID, "unloaded-chunk-in-swept-volume");
            }
            if (scan.specialBlockEncountered()) {
                return ModuleEvaluation.abstain(ID, "powder-snow-in-swept-volume");
            }
            return ModuleEvaluation.abstain(ID, "java-collision-in-swept-volume");
        }

        allowedMoves.increment();
        return ModuleEvaluation.allow(
                ID,
                "swimming-swept-volume-clear",
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
