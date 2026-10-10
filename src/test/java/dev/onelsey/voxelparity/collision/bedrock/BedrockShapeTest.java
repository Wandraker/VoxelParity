package dev.onelsey.voxelparity.collision.bedrock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.onelsey.voxelparity.collision.math.Aabb;
import org.junit.jupiter.api.Test;

class BedrockShapeTest {
    private static final double EPSILON = 0.000002D;

    @Test
    void bambooUsesStateAwareStalkWidths() {
        Aabb thin = BedrockBambooShape.worldBox(
                410, 161, 408, BedrockBambooShape.Thickness.THIN
        );
        Aabb thick = BedrockBambooShape.worldBox(
                410, 161, 408, BedrockBambooShape.Thickness.THICK
        );

        assertBambooWidth(thin, 2.0D / 16.0D);
        assertBambooWidth(thick, 3.0D / 16.0D);
        assertEquals(thin.minX(), thick.minX(), EPSILON);
        assertEquals(thin.minZ(), thick.minZ(), EPSILON);
    }

    @Test
    void javaBambooAgeMapsToBedrockThickness() {
        assertEquals(
                BedrockBambooShape.Thickness.THIN,
                BedrockBambooShape.fromJavaAge(0)
        );
        assertEquals(
                BedrockBambooShape.Thickness.THICK,
                BedrockBambooShape.fromJavaAge(1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> BedrockBambooShape.fromJavaAge(2)
        );
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

    private static void assertBambooWidth(Aabb box, double width) {
        double expected = width - 2.0D / 16384.0D;
        assertEquals(expected, box.maxX() - box.minX(), EPSILON);
        assertEquals(expected, box.maxZ() - box.minZ(), EPSILON);
        assertEquals(1.0D, box.maxY() - box.minY(), EPSILON);
    }

    private static void assertWidth(BedrockDripstoneShape.Thickness thickness, double width) {
        Aabb box = BedrockDripstoneShape.worldBox(0, 100, 0, thickness, false);
        double expected = width - 2.0D / 16384.0D;
        assertEquals(expected, box.maxX() - box.minX(), EPSILON);
        assertEquals(expected, box.maxZ() - box.minZ(), EPSILON);
    }
}
