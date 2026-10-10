package dev.onelsey.voxelparity.collision.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AcceptedMoveGuardTest {
    private static final double EPSILON = 0.0000001D;

    @Test
    void doesNotCorrectAFreeTargetThatOnlySweepsPastACorner() {
        Aabb start = new Aabb(
                -410.191200D, 71.0D, -407.179150D,
                -409.591200D, 72.8D, -406.579150D
        );
        Aabb target = new Aabb(
                -410.264000D, 71.0D, -407.086960D,
                -409.664000D, 72.8D, -406.486960D
        );
        Aabb obstacle = new Aabb(
                -409.595772D, 71.0D, -406.579106D,
                -409.220894D, 71.6875D, -406.204228D
        );

        ResolvedMotion result = AcceptedMoveGuard.resolve(
                start,
                target,
                List.of(obstacle)
        );

        assertFalse(result.collided());
        assertEquals(target.minX() - start.minX(), result.motion().x(), EPSILON);
        assertEquals(target.minZ() - start.minZ(), result.motion().z(), EPSILON);
    }

    @Test
    void correctsMovementThatEndsInsideBedrockCollision() {
        Aabb start = new Aabb(0.0D, 0.0D, 0.0D, 0.6D, 1.8D, 0.6D);
        Aabb target = start.move(new Motion(0.5D, 0.0D, 0.0D));
        Aabb obstacle = new Aabb(1.0D, 0.0D, 0.0D, 1.1875D, 1.0D, 0.6D);

        ResolvedMotion result = AcceptedMoveGuard.resolve(
                start,
                target,
                List.of(obstacle)
        );

        assertTrue(result.collided());
        assertEquals(0.4D, result.motion().x(), EPSILON);
    }

    @Test
    void allowsEscapeToAFreeTargetFromExistingOverlap() {
        Aabb start = new Aabb(0.0D, 0.0D, 0.0D, 0.6D, 1.8D, 0.6D);
        Aabb target = start.move(new Motion(-0.5D, 0.0D, 0.0D));
        Aabb obstacle = new Aabb(0.4D, 0.0D, 0.0D, 0.8D, 1.0D, 0.6D);

        ResolvedMotion result = AcceptedMoveGuard.resolve(
                start,
                target,
                List.of(obstacle)
        );

        assertFalse(result.collided());
        assertEquals(-0.5D, result.motion().x(), EPSILON);
    }
}
