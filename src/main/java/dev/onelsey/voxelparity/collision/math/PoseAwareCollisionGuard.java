package dev.onelsey.voxelparity.collision.math;

import java.util.List;
import java.util.Objects;

public final class PoseAwareCollisionGuard {
    private static final double EPSILON = 1.0E-7D;

    private PoseAwareCollisionGuard() {
    }

    public static boolean compactTargetClearsTranslatedCollision(
            Aabb normalTarget,
            Aabb compactTarget,
            List<Aabb> obstacles
    ) {
        Objects.requireNonNull(normalTarget, "normalTarget");
        Objects.requireNonNull(compactTarget, "compactTarget");
        Objects.requireNonNull(obstacles, "obstacles");

        if (!isSubset(normalTarget, compactTarget)) {
            return false;
        }

        boolean normalOverlaps = obstacles.stream().anyMatch(normalTarget::overlaps);
        if (!normalOverlaps) {
            return false;
        }

        return obstacles.stream().noneMatch(compactTarget::overlaps);
    }

    private static boolean isSubset(Aabb outer, Aabb inner) {
        return inner.minX() >= outer.minX() - EPSILON
                && inner.minY() >= outer.minY() - EPSILON
                && inner.minZ() >= outer.minZ() - EPSILON
                && inner.maxX() <= outer.maxX() + EPSILON
                && inner.maxY() <= outer.maxY() + EPSILON
                && inner.maxZ() <= outer.maxZ() + EPSILON;
    }
}
