package dev.onelsey.voxelparity.movement;

import org.bukkit.GameMode;

public final class MovementPolicy {
    private MovementPolicy() {
    }

    public static boolean bypassParity(GameMode gameMode) {
        return gameMode == GameMode.SPECTATOR;
    }
}
