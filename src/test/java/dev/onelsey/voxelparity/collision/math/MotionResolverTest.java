package dev.onelsey.voxelparity.collision.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MotionResolverTest {
    private static final double EPSILON = 0.0000001D;

    @Test
    void leavesClearMovementUntouched() {
        Aabb start = new Aabb(0.0D, 0.0D, 0.0D, 0.6D, 1.8D, 0.6D);
        Aabb target = start.move(new Motion(0.25D, 0.0D, 0.5D));

        ResolvedMotion result = MotionResolver.resolve(
                start,
                target,
                List.of(new Aabb(5.0D, 0.0D, 5.0D, 6.0D, 1.0D, 6.0D))
        );

        assertFalse(result.collided());
        assertEquals(0.25D, result.motion().x(), EPSILON);
        assertEquals(0.5D, result.motion().z(), EPSILON);
    }

    @Test
    void clipsHorizontalMovementAtObstacleFace() {
        Aabb start = new Aabb(0.0D, 0.0D, 0.0D, 0.6D, 1.8D, 0.6D);
        Aabb target = start.move(new Motion(0.8D, 0.0D, 0.0D));

        ResolvedMotion result = MotionResolver.resolve(
                start,
                target,
                List.of(new Aabb(1.0D, 0.0D, 0.0D, 1.2D, 1.0D, 0.6D))
        );

        assertTrue(result.collided());
        assertEquals(0.4D, result.motion().x(), EPSILON);
    }

    @Test
    void permitsMovementOutOfExistingOverlap() {
        Aabb start = new Aabb(0.0D, 0.0D, 0.0D, 0.6D, 1.8D, 0.6D);
        Aabb target = start.move(new Motion(-0.5D, 0.0D, 0.0D));

        ResolvedMotion result = MotionResolver.resolve(
                start,
                target,
                List.of(new Aabb(0.4D, 0.0D, 0.0D, 0.8D, 1.0D, 0.6D))
        );

        assertFalse(result.collided());
        assertEquals(-0.5D, result.motion().x(), EPSILON);
    }

    @Test
    void preventsCrossingAThinTranslatedCollisionInOneMove() {
        Aabb start = new Aabb(0.0D, 0.0D, 0.0D, 0.6D, 1.8D, 0.6D);
        Aabb target = start.move(new Motion(4.0D, 0.0D, 0.0D));

        ResolvedMotion result = MotionResolver.resolve(
                start,
                target,
                List.of(new Aabb(1.0D, 0.0D, 0.0D, 1.1875D, 1.0D, 0.6D))
        );

        assertTrue(result.collided());
        assertEquals(0.4D, result.motion().x(), EPSILON);
    }
}
