package dev.onelsey.voxelparity.collision.math;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class PoseAwareCollisionGuardTest {
    @Test
    void compactPoseClearsHangingDripstoneFromRuntimeTrace() {
        Aabb standingTarget = new Aabb(
                -403.186570D, 71.0D, -403.445840D,
                -402.586570D, 72.8D, -402.845840D
        );
        Aabb compactTarget = new Aabb(
                -403.186570D, 71.0D, -403.445840D,
                -402.586570D, 71.6D, -402.845840D
        );
        Aabb hangingDripstone = new Aabb(
                -402.629106D, 72.3125D, -403.629106D,
                -402.254228D, 73.0D, -403.254228D
        );

        assertTrue(PoseAwareCollisionGuard.compactTargetClearsTranslatedCollision(
                standingTarget,
                compactTarget,
                List.of(hangingDripstone)
        ));
    }

    @Test
    void compactPoseDoesNotBypassLateralCollision() {
        Aabb standingTarget = new Aabb(0.0D, 0.0D, 0.0D, 0.6D, 1.8D, 0.6D);
        Aabb compactTarget = new Aabb(0.0D, 0.0D, 0.0D, 0.6D, 0.6D, 0.6D);
        Aabb obstacle = new Aabb(0.4D, 0.0D, 0.0D, 0.8D, 1.0D, 0.6D);

        assertFalse(PoseAwareCollisionGuard.compactTargetClearsTranslatedCollision(
                standingTarget,
                compactTarget,
                List.of(obstacle)
        ));
    }

    @Test
    void compactPoseIsIgnoredWhenNormalTargetIsAlreadyClear() {
        Aabb standingTarget = new Aabb(0.0D, 0.0D, 0.0D, 0.6D, 1.8D, 0.6D);
        Aabb compactTarget = new Aabb(0.0D, 0.0D, 0.0D, 0.6D, 0.6D, 0.6D);
        Aabb obstacle = new Aabb(2.0D, 0.0D, 0.0D, 2.2D, 1.0D, 0.6D);

        assertFalse(PoseAwareCollisionGuard.compactTargetClearsTranslatedCollision(
                standingTarget,
                compactTarget,
                List.of(obstacle)
        ));
    }

    @Test
    void compactPoseMustBeContainedInsideNormalTarget() {
        Aabb standingTarget = new Aabb(0.0D, 0.0D, 0.0D, 0.6D, 1.8D, 0.6D);
        Aabb invalidCompactTarget = new Aabb(-0.2D, 0.0D, 0.0D, 0.6D, 0.6D, 0.6D);
        Aabb obstacle = new Aabb(0.4D, 1.0D, 0.0D, 0.8D, 1.6D, 0.6D);

        assertFalse(PoseAwareCollisionGuard.compactTargetClearsTranslatedCollision(
                standingTarget,
                invalidCompactTarget,
                List.of(obstacle)
        ));
    }
}
