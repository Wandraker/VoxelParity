package dev.onelsey.voxelparity.collision.bedrock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BedrockOffsetPatternTest {
    private static final double EPSILON = 0.000001D;

    @Test
    void reproducesKnownBambooOffsets() {
        assertOffset(410, 408, -0.25F, 0.25F, 0.18333334F, 0.08333333F);
        assertOffset(-8, -8, -0.25F, 0.25F, 0.11666667F, -0.01666667F);
        assertOffset(-1, 0, -0.25F, 0.25F, -0.08333334F, 0.25F);
    }

    @Test
    void dripstoneUsesTheSamePatternAtHalfRange() {
        BedrockOffsetPattern.Offset bamboo =
                BedrockOffsetPattern.sample(7, 11, -0.25F, 0.25F, 16);
        BedrockOffsetPattern.Offset dripstone =
                BedrockOffsetPattern.sample(7, 11, -0.125F, 0.125F, 16);

        assertEquals(bamboo.x() * 0.5F, dripstone.x(), EPSILON);
        assertEquals(bamboo.z() * 0.5F, dripstone.z(), EPSILON);
    }

    private static void assertOffset(
            int x,
            int z,
            float minimum,
            float maximum,
            float expectedX,
            float expectedZ
    ) {
        BedrockOffsetPattern.Offset actual =
                BedrockOffsetPattern.sample(x, z, minimum, maximum, 16);
        assertEquals(expectedX, actual.x(), EPSILON);
        assertEquals(expectedZ, actual.z(), EPSILON);
    }
}
