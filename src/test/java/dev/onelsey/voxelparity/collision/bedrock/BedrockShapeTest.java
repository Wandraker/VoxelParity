package dev.onelsey.voxelparity.collision.bedrock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.onelsey.voxelparity.collision.math.Aabb;
import org.junit.jupiter.api.Test;

class BedrockShapeTest {
    private static final double EPSILON = 0.000002D;

    @Test
    void bambooUsesMeasuredStalkWidth() {
        Aabb box = BedrockBambooShape.worldBox(410, 161, 408);
        double expected = 3.0D / 16.0D - 2.0D / 16384.0D;

        assertEquals(expected, box.maxX() - box.minX(), EPSILON);
        assertEquals(expected, box.maxZ() - box.minZ(), EPSILON);
        assertEquals(1.0D, box.maxY() - box.minY(), EPSILON);
    }

    @Test
    void dripstoneWidthsMatchThickness() {
        assertWidth(BedrockDripstoneShape.Thickness.TIP, 6.0D / 16.0D);
        assertWidth(BedrockDripstoneShape.Thickness.TIP_MERGE, 6.0D / 16.0D);
        assertWidth(BedrockDripstoneShape.Thickness.FRUSTUM, 8.0D / 16.0D);
        assertWidth(BedrockDripstoneShape.Thickness.MIDDLE, 10.0D / 16.0D);
        assertWidth(BedrockDripstoneShape.Thickness.BASE, 12.0D / 16.0D);
    }

    @Test
    void tipHeightDependsOnOrientation() {
        Aabb upward = BedrockDripstoneShape.worldBox(
                0, 100, 0, BedrockDripstoneShape.Thickness.TIP, false
        );
        Aabb hanging = BedrockDripstoneShape.worldBox(
                0, 100, 0, BedrockDripstoneShape.Thickness.TIP, true
        );

        assertEquals(100.0D, upward.minY(), EPSILON);
        assertEquals(100.0D + 11.0D / 16.0D, upward.maxY(), EPSILON);
        assertEquals(100.0D + 5.0D / 16.0D, hanging.minY(), EPSILON);
        assertEquals(101.0D, hanging.maxY(), EPSILON);
    }

    private static void assertWidth(BedrockDripstoneShape.Thickness thickness, double width) {
        Aabb box = BedrockDripstoneShape.worldBox(0, 100, 0, thickness, false);
        double expected = width - 2.0D / 16384.0D;
        assertEquals(expected, box.maxX() - box.minX(), EPSILON);
        assertEquals(expected, box.maxZ() - box.minZ(), EPSILON);
    }
}
