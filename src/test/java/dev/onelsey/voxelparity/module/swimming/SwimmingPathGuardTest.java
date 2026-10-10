package dev.onelsey.voxelparity.module.swimming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.onelsey.voxelparity.collision.math.Aabb;
import java.util.List;
import org.junit.jupiter.api.Test;

class SwimmingPathGuardTest {
    private static final Aabb START =
            new Aabb(0.0D, 0.0D, 0.0D, 0.6D, 0.6D, 0.6D);
    private static final Aabb TARGET =
            new Aabb(0.2D, 0.0D, 0.2D, 0.8D, 0.6D, 0.8D);

    @Test
    void clearPathIsAllowed() {
        assertTrue(SwimmingPathGuard.canAllow(START, TARGET, List.of()));
    }

    @Test
    void wallCrossingIsRejected() {
        Aabb wall = new Aabb(
                0.7D, -1.0D, -1.0D,
                1.0D, 1.0D, 1.0D
        );

        assertFalse(SwimmingPathGuard.canAllow(START, TARGET, List.of(wall)));
    }

    @Test
    void sweptUnionCornerDoesNotCreateFalseCollision() {
        Aabb corner = new Aabb(
                -0.1D, -1.0D, 0.7D,
                0.1D, 1.0D, 0.9D
        );

        assertTrue(START.union(TARGET).overlaps(corner));
        assertTrue(SwimmingPathGuard.canAllow(START, TARGET, List.of(corner)));
    }

    @Test
    void targetOverlapIsRejected() {
        Aabb obstacle = new Aabb(
                0.5D, -1.0D, 0.5D,
                0.9D, 1.0D, 0.9D
        );

        assertFalse(SwimmingPathGuard.canAllow(START, TARGET, List.of(obstacle)));
    }

    @Test
    void standingBoxCanBeCompactedToSwimmingHeight() {
        Aabb standing = new Aabb(
                1.0D, 5.0D, 2.0D,
                1.6D, 6.8D, 2.6D
        );

        Aabb compact = SwimmingPathGuard.compactAtFeet(standing, 0.6D);

        assertEquals(5.0D, compact.minY(), 1.0E-9D);
        assertEquals(5.6D, compact.maxY(), 1.0E-9D);
        assertEquals(standing.minX(), compact.minX(), 1.0E-9D);
        assertEquals(standing.maxZ(), compact.maxZ(), 1.0E-9D);
    }
}
