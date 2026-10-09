package dev.onelsey.voxelparity.collision.paper;

import dev.onelsey.voxelparity.collision.math.Aabb;
import dev.onelsey.voxelparity.collision.math.Motion;
import dev.onelsey.voxelparity.collision.math.MotionResolver;
import dev.onelsey.voxelparity.collision.math.ResolvedMotion;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public final class CollisionParityService {
    private final LongAdder allowedFailedMoves = new LongAdder();
    private final LongAdder correctedAcceptedMoves = new LongAdder();
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

    public FailedMoveDecision evaluateFailedMove(Player player, Aabb target) {
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
            blockedFailedCandidates.increment();
            return FailedMoveDecision.blocked("bedrock-collision-still-overlaps");
        }

        allowedFailedMoves.increment();
        return FailedMoveDecision.allow("java-bedrock-collision-mismatch");
    }

    public Optional<Location> correctAcceptedMove(Player player, Location from, Location to) {
        Motion requested = new Motion(
                to.getX() - from.getX(),
                to.getY() - from.getY(),
                to.getZ() - from.getZ()
        );
        if (tooLarge(requested)) {
            return Optional.empty();
        }

        Aabb start = PaperBoxes.playerAt(player, from);
        Aabb target = PaperBoxes.playerAt(player, to);
        CollisionWorld.CollisionBoxes collisionBoxes = CollisionWorld.translatedBoxes(
                player.getWorld(),
                start.union(target),
                translated
        );
        if (collisionBoxes.unloadedChunk() || collisionBoxes.boxes().isEmpty()) {
            return Optional.empty();
        }

        ResolvedMotion resolved = MotionResolver.resolve(start, target, collisionBoxes.boxes());
        if (!resolved.collided()) {
            return Optional.empty();
        }

        Motion motion = resolved.motion();
        Location corrected = to.clone();
        corrected.setX(from.getX() + motion.x());
        corrected.setY(from.getY() + motion.y());
        corrected.setZ(from.getZ() + motion.z());
        correctedAcceptedMoves.increment();
        return Optional.of(corrected);
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

    public long blockedFailedCandidates() {
        return blockedFailedCandidates.sum();
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
}
