package dev.onelsey.voxelparity.collision.paper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

class CollisionMetricsTest {
    @Test
    void countersStaySeparatedByTranslatedModule() {
        CollisionMetrics metrics = new CollisionMetrics();

        metrics.recordAllowedFailed(Set.of(TranslatedCollisionKind.BAMBOO));
        metrics.recordCorrectedAccepted(Set.of(TranslatedCollisionKind.POINTED_DRIPSTONE));
        metrics.recordPoseAccepted(Set.of(TranslatedCollisionKind.POINTED_DRIPSTONE));
        metrics.recordPoseFailedAllowed(Set.of(TranslatedCollisionKind.POINTED_DRIPSTONE));
        metrics.recordBlockedFailed(Set.of(
                TranslatedCollisionKind.BAMBOO,
                TranslatedCollisionKind.POINTED_DRIPSTONE
        ));

        CollisionMetrics.Snapshot bamboo =
                metrics.snapshot(TranslatedCollisionKind.BAMBOO);
        CollisionMetrics.Snapshot dripstone =
                metrics.snapshot(TranslatedCollisionKind.POINTED_DRIPSTONE);

        assertEquals(1L, bamboo.allowedFailed());
        assertEquals(0L, bamboo.correctedAccepted());
        assertEquals(0L, bamboo.poseAccepted());
        assertEquals(0L, bamboo.poseFailedAllowed());
        assertEquals(1L, bamboo.blockedFailed());

        assertEquals(0L, dripstone.allowedFailed());
        assertEquals(1L, dripstone.correctedAccepted());
        assertEquals(1L, dripstone.poseAccepted());
        assertEquals(1L, dripstone.poseFailedAllowed());
        assertEquals(1L, dripstone.blockedFailed());
    }
}
