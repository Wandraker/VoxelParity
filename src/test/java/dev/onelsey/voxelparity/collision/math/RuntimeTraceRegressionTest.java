package dev.onelsey.voxelparity.collision.math;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RuntimeTraceRegressionTest {
    @Test
    void legalDripstoneCornerTargetFromRuntimeTraceIsNotCorrected() {
        Aabb start = new Aabb(
                -410.191200D, 71.0D, -407.179150D,
                -409.591200D, 72.8D, -406.579150D
        );
        Aabb target = new Aabb(
                -410.264000D, 71.0D, -407.086960D,
                -409.664000D, 72.8D, -406.486960D
        );
        Aabb dripstone = new Aabb(
                -409.595772D, 71.0D, -406.579106D,
                -409.220894D, 71.6875D, -406.204228D
        );

        assertFalse(AcceptedMoveGuard.resolve(
                start,
                target,
                List.of(dripstone)
        ).collided());
    }

    @Test
    void compactPoseFromRuntimeTraceClearsHangingTip() {
        Aabb standingTarget = new Aabb(
                -402.111650D, 71.0D, -403.270300D,
                -401.511650D, 72.8D, -402.670300D
        );
        Aabb compactTarget = new Aabb(
                -402.111650D, 71.0D, -403.270300D,
                -401.511650D, 71.6D, -402.670300D
        );
        Aabb hangingTip = new Aabb(
                -401.679106D, 72.3125D, -402.729106D,
                -401.304228D, 73.0D, -402.354228D
        );

        assertTrue(PoseAwareCollisionGuard.compactTargetClearsTranslatedCollision(
                standingTarget,
                compactTarget,
                List.of(hangingTip)
        ));
    }
}
