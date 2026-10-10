package dev.onelsey.voxelparity.module.swimming;

import dev.onelsey.voxelparity.collision.math.Aabb;
import dev.onelsey.voxelparity.collision.math.MotionResolver;
import java.util.List;
import java.util.Objects;

public final class SwimmingPathGuard {
    private SwimmingPathGuard() {
    }

    public static boolean canAllow(
            Aabb start,
            Aabb target,
            List<Aabb> obstacles
    ) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(obstacles, "obstacles");

        if (obstacles.stream().anyMatch(target::overlaps)) {
            return false;
        }

        return !MotionResolver.resolve(start, target, obstacles).collided();
    }

    public static Aabb compactAtFeet(Aabb box, double height) {
        Objects.requireNonNull(box, "box");
        double clampedHeight = Math.max(0.0D, Math.min(height, box.maxY() - box.minY()));
        return new Aabb(
                box.minX(),
                box.minY(),
                box.minZ(),
                box.maxX(),
                box.minY() + clampedHeight,
                box.maxZ()
        );
    }
}
