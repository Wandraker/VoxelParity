package dev.onelsey.voxelparity.collision.bedrock;

import dev.onelsey.voxelparity.collision.math.Aabb;

public final class BedrockBambooShape {
    private static final double FACE_EPSILON = 1.0D / 16384.0D;
    private static final double WIDTH = 3.0D / 16.0D;

    private BedrockBambooShape() {
    }

    public static Aabb worldBox(int x, int y, int z) {
        BedrockOffsetPattern.Offset offset =
                BedrockOffsetPattern.sample(x, z, -0.25F, 0.25F, 16);

        double minX = x + 0.5D + offset.x();
        double minZ = z + 0.5D + offset.z();
        return new Aabb(
                minX + FACE_EPSILON,
                y,
                minZ + FACE_EPSILON,
                minX + WIDTH - FACE_EPSILON,
                y + 1.0D,
                minZ + WIDTH - FACE_EPSILON
        );
    }
}
