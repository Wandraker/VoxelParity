package dev.onelsey.voxelparity.collision.paper;

import dev.onelsey.voxelparity.collision.math.Aabb;
import dev.onelsey.voxelparity.collision.math.AcceptedMoveGuard;
import dev.onelsey.voxelparity.collision.math.Motion;
import dev.onelsey.voxelparity.collision.math.PoseAwareCollisionGuard;
import dev.onelsey.voxelparity.collision.math.ResolvedMotion;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public final class CollisionParityService {
    private final LongAdder allowedFailedMoves = new LongAdder();
    private final LongAdder correctedAcceptedMoves = new LongAdder();
    private final LongAdder poseCompatibleAcceptedMoves = new LongAdder();
    private final LongAdder poseCompatibleFailedMoves = new LongAdder();
    private final LongAdder blockedFailedCandidates = new LongAdder();

    private volatile TranslatedBlockCollision translated = new TranslatedBlockCollision(false, false);
    private volatile double maxMovementPerAxis = 8.0D;

    public void reload(FileConfiguration config) {
        translated = new TranslatedBlockCollision(
                config.getBoolean("modules.bamboo.enabled", true),
                config.getBoolean("modules.pointed-dripstone.enabled", true)
        );
        maxMovementPerAxis = Math.max(
                0.1D,
                config.getDouble("collision.max-movement-per-axis", 8.0D)
        );
    }

    public FailedMoveDecision evaluateFailedMove(
            Player player,
            Aabb target,
            Optional<Aabb> compactPoseTarget
    ) {
        CollisionWorld.TargetScan scan = CollisionWorld.scanTarget(player, target, translated);
        if (!scan.javaTranslatedOverlap()) {
            return FailedMoveDecision.abstain("no-java-translated-collision");
        }

        if (scan.unloadedChunk()) {
            blockedFailedCandidates.increment();
            return FailedMoveDecision.blocked("unloaded-chunk-in-target");
        }
        if (scan.otherBlockCollision()) {
            blockedFailedCandidates.increment();
            return FailedMoveDecision.blocked("other-block-collision");
        }
        if (scan.entityCollision()) {
            blockedFailedCandidates.increment();
            return FailedMoveDecision.blocked("entity-collision");
        }
        if (scan.bedrockTranslatedOverlap()) {
            if (compactPoseTarget.isPresent()
                    && compactPoseTargetIsSafe(player, compactPoseTarget.get())) {
                allowedFailedMoves.increment();
                poseCompatibleFailedMoves.increment();
                return FailedMoveDecision.allow("recent-swimming-target-clear");
            }

            blockedFailedCandidates.increment();
            return FailedMoveDecision.blocked("bedrock-collision-still-overlaps");
        }

        allowedFailedMoves.increment();
        return FailedMoveDecision.allow("java-bedrock-collision-mismatch");
    }

    public AcceptedMoveDecision evaluateAcceptedMove(
            Player player,
            Location from,
            Location to,
            Optional<Aabb> compactPoseTarget
    ) {
        Motion requested = new Motion(
                to.getX() - from.getX(),
                to.getY() - from.getY(),
                to.getZ() - from.getZ()
        );
        if (tooLarge(requested)) {
            return AcceptedMoveDecision.abstain("movement-exceeds-collision-limit");
        }

        Aabb start = PaperBoxes.playerAt(player, from);
        Aabb target = PaperBoxes.playerAt(player, to);
        CollisionWorld.CollisionBoxes collisionBoxes = CollisionWorld.translatedBoxes(
                player.getWorld(),
                start.union(target),
                translated
        );
        if (collisionBoxes.unloadedChunk()) {
            return AcceptedMoveDecision.abstain("unloaded-chunk-in-swept-volume");
        }
        if (collisionBoxes.boxes().isEmpty()) {
            return AcceptedMoveDecision.abstain("no-translated-collision-nearby");
        }

        if (compactPoseTarget.isPresent()
                && PoseAwareCollisionGuard.compactTargetClearsTranslatedCollision(
                        target,
                        compactPoseTarget.get(),
                        collisionBoxes.boxes()
                )) {
            poseCompatibleAcceptedMoves.increment();
            return AcceptedMoveDecision.poseCompatible(
                    "recent-swimming-target-clear",
                    compactPoseTarget.get()
            );
        }

        ResolvedMotion resolved = AcceptedMoveGuard.resolve(start, target, collisionBoxes.boxes());
        if (!resolved.collided()) {
            return AcceptedMoveDecision.abstain("target-clear");
        }

        Motion motion = resolved.motion();
        Location corrected = to.clone();
        corrected.setX(from.getX() + motion.x());
        corrected.setY(from.getY() + motion.y());
        corrected.setZ(from.getZ() + motion.z());
        correctedAcceptedMoves.increment();
        return AcceptedMoveDecision.corrected("bedrock-target-overlap", corrected);
    }

    public List<CollisionWorld.TranslatedCollisionDiagnostic> diagnostics(
            Player player,
            Aabb bounds,
            Aabb target
    ) {
        return CollisionWorld.translatedDiagnostics(
                player.getWorld(),
                bounds,
                target,
                translated
        );
    }

    public boolean bambooEnabled() {
        return translated.bambooEnabled();
    }

    public boolean pointedDripstoneEnabled() {
        return translated.pointedDripstoneEnabled();
    }

    public long allowedFailedMoves() {
        return allowedFailedMoves.sum();
    }

    public long correctedAcceptedMoves() {
        return correctedAcceptedMoves.sum();
    }

    public long poseCompatibleAcceptedMoves() {
        return poseCompatibleAcceptedMoves.sum();
    }

    public long poseCompatibleFailedMoves() {
        return poseCompatibleFailedMoves.sum();
    }

    public long blockedFailedCandidates() {
        return blockedFailedCandidates.sum();
    }

    private boolean compactPoseTargetIsSafe(Player player, Aabb compactTarget) {
        CollisionWorld.TargetScan compactScan =
                CollisionWorld.scanTarget(player, compactTarget, translated);
        return !compactScan.unloadedChunk()
                && !compactScan.otherBlockCollision()
                && !compactScan.entityCollision()
                && !compactScan.bedrockTranslatedOverlap();
    }

    private boolean tooLarge(Motion motion) {
        return Math.abs(motion.x()) > maxMovementPerAxis
                || Math.abs(motion.y()) > maxMovementPerAxis
                || Math.abs(motion.z()) > maxMovementPerAxis;
    }

    public record FailedMoveDecision(Outcome outcome, String reason) {
        public enum Outcome {
            ABSTAIN,
            BLOCKED,
            ALLOW
        }

        public static FailedMoveDecision abstain(String reason) {
            return new FailedMoveDecision(Outcome.ABSTAIN, reason);
        }

        public static FailedMoveDecision blocked(String reason) {
            return new FailedMoveDecision(Outcome.BLOCKED, reason);
        }

        public static FailedMoveDecision allow(String reason) {
            return new FailedMoveDecision(Outcome.ALLOW, reason);
        }
    }

    public record AcceptedMoveDecision(
            Outcome outcome,
            String reason,
            Optional<Location> correctedLocation,
            Optional<Aabb> compactTarget
    ) {
        public enum Outcome {
            ABSTAIN,
            CORRECT,
            POSE_COMPATIBLE
        }

        public static AcceptedMoveDecision abstain(String reason) {
            return new AcceptedMoveDecision(
                    Outcome.ABSTAIN,
                    reason,
                    Optional.empty(),
                    Optional.empty()
            );
        }

        public static AcceptedMoveDecision corrected(String reason, Location location) {
            return new AcceptedMoveDecision(
                    Outcome.CORRECT,
                    reason,
                    Optional.of(location),
                    Optional.empty()
            );
        }

        public static AcceptedMoveDecision poseCompatible(String reason, Aabb compactTarget) {
            return new AcceptedMoveDecision(
                    Outcome.POSE_COMPATIBLE,
                    reason,
                    Optional.empty(),
                    Optional.of(compactTarget)
            );
        }
    }
}
