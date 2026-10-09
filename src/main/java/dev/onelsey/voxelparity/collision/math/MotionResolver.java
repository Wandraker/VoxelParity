package dev.onelsey.voxelparity.collision.math;

import java.util.List;

public final class MotionResolver {
    private MotionResolver() {
    }

    public static ResolvedMotion resolve(Aabb start, Aabb target, List<Aabb> obstacles) {
        Motion requested = new Motion(
                target.minX() - start.minX(),
                target.minY() - start.minY(),
                target.minZ() - start.minZ()
        );

        double y = clipY(start, requested.y(), obstacles);
        Aabb afterY = start.move(new Motion(0.0D, y, 0.0D));

        double x = requested.x();
        double z = requested.z();
        if (Math.abs(x) >= Math.abs(z)) {
            x = clipX(afterY, x, obstacles);
            Aabb afterX = afterY.move(new Motion(x, 0.0D, 0.0D));
            z = clipZ(afterX, z, obstacles);
        } else {
            z = clipZ(afterY, z, obstacles);
            Aabb afterZ = afterY.move(new Motion(0.0D, 0.0D, z));
            x = clipX(afterZ, x, obstacles);
        }

        Motion resolved = new Motion(x, y, z);
        boolean collided = Double.compare(x, requested.x()) != 0
                || Double.compare(y, requested.y()) != 0
                || Double.compare(z, requested.z()) != 0;
        return new ResolvedMotion(resolved, collided);
    }

    private static double clipX(Aabb moving, double amount, List<Aabb> obstacles) {
        double result = amount;
        for (Aabb obstacle : obstacles) {
            if (!overlapsYZ(moving, obstacle)) {
                continue;
            }
            if (result > 0.0D && moving.maxX() <= obstacle.minX()) {
                result = Math.min(result, obstacle.minX() - moving.maxX());
            } else if (result < 0.0D && moving.minX() >= obstacle.maxX()) {
                result = Math.max(result, obstacle.maxX() - moving.minX());
            }
        }
        return result;
    }

    private static double clipY(Aabb moving, double amount, List<Aabb> obstacles) {
        double result = amount;
        for (Aabb obstacle : obstacles) {
            if (!overlapsXZ(moving, obstacle)) {
                continue;
            }
            if (result > 0.0D && moving.maxY() <= obstacle.minY()) {
                result = Math.min(result, obstacle.minY() - moving.maxY());
            } else if (result < 0.0D && moving.minY() >= obstacle.maxY()) {
                result = Math.max(result, obstacle.maxY() - moving.minY());
            }
        }
        return result;
    }

    private static double clipZ(Aabb moving, double amount, List<Aabb> obstacles) {
        double result = amount;
        for (Aabb obstacle : obstacles) {
            if (!overlapsXY(moving, obstacle)) {
                continue;
            }
            if (result > 0.0D && moving.maxZ() <= obstacle.minZ()) {
                result = Math.min(result, obstacle.minZ() - moving.maxZ());
            } else if (result < 0.0D && moving.minZ() >= obstacle.maxZ()) {
                result = Math.max(result, obstacle.maxZ() - moving.minZ());
            }
        }
        return result;
    }

    private static boolean overlapsYZ(Aabb a, Aabb b) {
        return a.maxY() > b.minY() && a.minY() < b.maxY()
                && a.maxZ() > b.minZ() && a.minZ() < b.maxZ();
    }

    private static boolean overlapsXZ(Aabb a, Aabb b) {
        return a.maxX() > b.minX() && a.minX() < b.maxX()
                && a.maxZ() > b.minZ() && a.minZ() < b.maxZ();
    }

    private static boolean overlapsXY(Aabb a, Aabb b) {
        return a.maxX() > b.minX() && a.minX() < b.maxX()
                && a.maxY() > b.minY() && a.minY() < b.maxY();
    }
}
