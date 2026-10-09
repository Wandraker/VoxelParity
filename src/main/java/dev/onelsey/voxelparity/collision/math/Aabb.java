package dev.onelsey.voxelparity.collision.math;

public record Aabb(
        double minX,
        double minY,
        double minZ,
        double maxX,
        double maxY,
        double maxZ
) {
    public Aabb {
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException("AABB minimums must not exceed maximums");
        }
    }

    public boolean overlaps(Aabb other) {
        return maxX > other.minX
                && minX < other.maxX
                && maxY > other.minY
                && minY < other.maxY
                && maxZ > other.minZ
                && minZ < other.maxZ;
    }

    public Aabb move(Motion motion) {
        return new Aabb(
                minX + motion.x(),
                minY + motion.y(),
                minZ + motion.z(),
                maxX + motion.x(),
                maxY + motion.y(),
                maxZ + motion.z()
        );
    }

    public Aabb union(Aabb other) {
        return new Aabb(
                Math.min(minX, other.minX),
                Math.min(minY, other.minY),
                Math.min(minZ, other.minZ),
                Math.max(maxX, other.maxX),
                Math.max(maxY, other.maxY),
                Math.max(maxZ, other.maxZ)
        );
    }
}
