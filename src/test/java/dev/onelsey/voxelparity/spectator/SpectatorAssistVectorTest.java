package dev.onelsey.voxelparity.spectator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpectatorAssistVectorTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void coversAllSixMovementDirections() {
        assertDirection(
                new SpectatorInputState(true, false, false, false, false, false, false),
                0.0D, 0.0D, 1.0D
        );
        assertDirection(
                new SpectatorInputState(false, true, false, false, false, false, false),
                0.0D, 0.0D, -1.0D
        );
        assertDirection(
                new SpectatorInputState(false, false, true, false, false, false, false),
                1.0D, 0.0D, 0.0D
        );
        assertDirection(
                new SpectatorInputState(false, false, false, true, false, false, false),
                -1.0D, 0.0D, 0.0D
        );
        assertDirection(
                new SpectatorInputState(false, false, false, false, true, false, false),
                0.0D, 1.0D, 0.0D
        );
        assertDirection(
                new SpectatorInputState(false, false, false, false, false, true, false),
                0.0D, -1.0D, 0.0D
        );
    }

    @Test
    void diagonalInputIsNormalized() {
        SpectatorAssistVector.Direction direction = SpectatorAssistVector.direction(
                new SpectatorInputState(true, false, false, true, true, false, false),
                0.0F,
                0.0F
        );

        double length = Math.sqrt(
                direction.x() * direction.x()
                        + direction.y() * direction.y()
                        + direction.z() * direction.z()
        );
        assertEquals(1.0D, length, EPSILON);
    }

    @Test
    void forwardMovementRespectsPitch() {
        SpectatorAssistVector.Direction direction = SpectatorAssistVector.direction(
                new SpectatorInputState(true, false, false, false, false, false, false),
                0.0F,
                90.0F
        );

        assertTrue(direction.y() < -0.999D);
    }

    private static void assertDirection(
            SpectatorInputState input,
            double x,
            double y,
            double z
    ) {
        SpectatorAssistVector.Direction direction =
                SpectatorAssistVector.direction(input, 0.0F, 0.0F);
        assertEquals(x, direction.x(), EPSILON);
        assertEquals(y, direction.y(), EPSILON);
        assertEquals(z, direction.z(), EPSILON);
    }
}
