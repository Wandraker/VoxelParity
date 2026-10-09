package dev.onelsey.voxelparity.collision.bedrock;

public final class BedrockOffsetPattern {
    private static final long SEED_STEP = -7046029254386353131L;

    private BedrockOffsetPattern() {
    }

    public static Offset sample(int blockX, int blockZ, float minimum, float maximum, int steps) {
        long seed = positionSeed(blockX, blockZ);
        Random128 random = new Random128(scramble(seed), scramble(seed + SEED_STEP));

        float x = quantize(random.nextLong(), minimum, maximum, steps);
        random.nextLong();
        float z = quantize(random.nextLong(), minimum, maximum, steps);
        return new Offset(x, z);
    }

    private static long positionSeed(int x, int z) {
        long mixedCoordinates = (116_129_781L * z)
                ^ ((0x2FC20F00000001L * Integer.toUnsignedLong(x)) >> 32);
        long folded = (mixedCoordinates * (42_317_861L * mixedCoordinates + 11L)) >>> 16;
        return ((int) folded) ^ 0x6A09E667F3BCC909L;
    }

    private static long scramble(long value) {
        value = (value ^ (value >>> 30)) * -4658895280553007687L;
        value = (value ^ (value >>> 27)) * -7723592293110705685L;
        return value ^ (value >>> 31);
    }

    private static float quantize(long value, float minimum, float maximum, int steps) {
        float unit = (value >>> 40) * 5.9604645E-8F;
        int index = Math.min(steps - 1, (int) Math.floor(unit * steps));
        float stride = (maximum - minimum) / (steps - 1);
        return minimum + stride * index;
    }

    public record Offset(float x, float z) {
    }

    private static final class Random128 {
        private long low;
        private long high;

        private Random128(long low, long high) {
            this.low = low;
            this.high = high;
        }

        private long nextLong() {
            long a = low;
            long b = high;
            long result = Long.rotateLeft(a + b, 17) + a;
            b ^= a;
            low = Long.rotateLeft(a, 49) ^ b ^ (b << 21);
            high = Long.rotateLeft(b, 28);
            return result;
        }
    }
}
