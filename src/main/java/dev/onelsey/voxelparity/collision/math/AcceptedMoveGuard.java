package dev.onelsey.voxelparity.collision.math;

import java.util.List;
import java.util.Objects;

public final class AcceptedMoveGuard {
    private AcceptedMoveGuard() {
    }

    public static ResolvedMotion resolve(
            Aabb start,
            Aabb target,
            List<Aabb> obstacles
    ) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(obstacles, "obstacles");

        Motion requested = new Motion(
                target.minX() - start.minX(),
                target.minY() - start.minY(),
                target.minZ() - start.minZ()
        );

        boolean targetOverlapsBedrockCollision = obstacles.stream().anyMatch(target::overlaps);
        if (!targetOverlapsBedrockCollision) {
            return new ResolvedMotion(requested, false);
        }

        return MotionResolver.resolve(start, target, obstacles);
    }
}
