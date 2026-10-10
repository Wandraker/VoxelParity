package dev.onelsey.voxelparity.spectator;

public final class SpectatorAssistVector {
    private static final double EPSILON = 1.0E-12D;

    private SpectatorAssistVector() {
    }

    public static Direction direction(
            SpectatorInputState input,
            float yawDegrees,
            float pitchDegrees
    ) {
        if (!input.hasMovement()) {
            return Direction.ZERO;
        }

        double yaw = Math.toRadians(yawDegrees);
        double pitch = Math.toRadians(pitchDegrees);

        double cosPitch = Math.cos(pitch);
        double forwardX = -cosPitch * Math.sin(yaw);
        double forwardY = -Math.sin(pitch);
        double forwardZ = cosPitch * Math.cos(yaw);

        double rightX = -Math.cos(yaw);
        double rightZ = -Math.sin(yaw);

        double forwardAmount = axis(input.forward(), input.backward());
        double rightAmount = axis(input.right(), input.left());
        double verticalAmount = axis(input.jump(), input.sneak());

        double x = forwardX * forwardAmount + rightX * rightAmount;
        double y = forwardY * forwardAmount + verticalAmount;
        double z = forwardZ * forwardAmount + rightZ * rightAmount;

        double length = Math.sqrt(x * x + y * y + z * z);
        if (length <= EPSILON) {
            return Direction.ZERO;
        }

        return new Direction(x / length, y / length, z / length);
    }

    private static int axis(boolean positive, boolean negative) {
        return (positive ? 1 : 0) - (negative ? 1 : 0);
    }

    public record Direction(double x, double y, double z) {
        public static final Direction ZERO = new Direction(0.0D, 0.0D, 0.0D);

        public boolean isZero() {
            return Math.abs(x) <= EPSILON
                    && Math.abs(y) <= EPSILON
                    && Math.abs(z) <= EPSILON;
        }
    }
}
