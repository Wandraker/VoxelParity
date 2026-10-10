package dev.onelsey.voxelparity.module.swimming;

public final class SwimmingClipGuard {
    private SwimmingClipGuard() {
    }

    public static boolean canAllow(
            boolean unloadedChunk,
            boolean powderSnow,
            boolean ordinaryCollision
    ) {
        return !unloadedChunk && !powderSnow && !ordinaryCollision;
    }
}
