package dev.onelsey.voxelparity.collision.bedrock;

import dev.onelsey.voxelparity.collision.math.Aabb;

public final class BedrockDripstoneShape {
    private static final double FACE_EPSILON = 1.0D / 16384.0D;

    private BedrockDripstoneShape() {
    }

    public static Aabb worldBox(int x, int y, int z, Thickness thickness, boolean hanging) {
        BedrockOffsetPattern.Offset offset =
                BedrockOffsetPattern.sample(x, z, -0.125F, 0.125F, 16);

        double centerX = x + 0.5D + offset.x();
        double centerZ = z + 0.5D + offset.z();
        double halfWidth = thickness.width / 2.0D;
        double minY = y;
        double maxY = y + 1.0D;

        if (thickness == Thickness.TIP) {
            if (hanging) {
                minY += 5.0D / 16.0D;
            } else {
                maxY -= 5.0D / 16.0D;
            }
        }

        return new Aabb(
                centerX - halfWidth + FACE_EPSILON,
                minY,
                centerZ - halfWidth + FACE_EPSILON,
                centerX + halfWidth - FACE_EPSILON,
                maxY,
                centerZ + halfWidth - FACE_EPSILON
        );
    }

    public enum Thickness {
        TIP(6.0D / 16.0D),
        TIP_MERGE(6.0D / 16.0D),
        FRUSTUM(8.0D / 16.0D),
        MIDDLE(10.0D / 16.0D),
        BASE(12.0D / 16.0D);

        private final double width;

        Thickness(double width) {
            this.width = width;
        }
    }
}
