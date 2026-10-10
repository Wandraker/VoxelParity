package dev.onelsey.voxelparity.collision.paper;

import java.util.EnumMap;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

public final class CollisionMetrics {
    private final EnumMap<TranslatedCollisionKind, Counters> counters =
            new EnumMap<>(TranslatedCollisionKind.class);

    public CollisionMetrics() {
        for (TranslatedCollisionKind kind : TranslatedCollisionKind.values()) {
            counters.put(kind, new Counters());
        }
    }

    public void recordAllowedFailed(Set<TranslatedCollisionKind> kinds) {
        kinds.forEach(kind -> counters.get(kind).allowedFailed.increment());
    }

    public void recordCorrectedAccepted(Set<TranslatedCollisionKind> kinds) {
        kinds.forEach(kind -> counters.get(kind).correctedAccepted.increment());
    }

    public void recordPoseAccepted(Set<TranslatedCollisionKind> kinds) {
        kinds.forEach(kind -> counters.get(kind).poseAccepted.increment());
    }

    public void recordPoseFailedAllowed(Set<TranslatedCollisionKind> kinds) {
        kinds.forEach(kind -> counters.get(kind).poseFailedAllowed.increment());
    }

    public void recordBlockedFailed(Set<TranslatedCollisionKind> kinds) {
        kinds.forEach(kind -> counters.get(kind).blockedFailed.increment());
    }

    public Snapshot snapshot(TranslatedCollisionKind kind) {
        Counters value = counters.get(kind);
        return new Snapshot(
                value.allowedFailed.sum(),
                value.correctedAccepted.sum(),
                value.poseAccepted.sum(),
                value.poseFailedAllowed.sum(),
                value.blockedFailed.sum()
        );
    }

    private static final class Counters {
        private final LongAdder allowedFailed = new LongAdder();
        private final LongAdder correctedAccepted = new LongAdder();
        private final LongAdder poseAccepted = new LongAdder();
        private final LongAdder poseFailedAllowed = new LongAdder();
        private final LongAdder blockedFailed = new LongAdder();
    }

    public record Snapshot(
            long allowedFailed,
            long correctedAccepted,
            long poseAccepted,
            long poseFailedAllowed,
            long blockedFailed
    ) {
    }
}
