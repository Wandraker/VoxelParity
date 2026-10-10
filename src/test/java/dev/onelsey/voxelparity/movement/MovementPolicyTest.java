package dev.onelsey.voxelparity.movement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.GameMode;
import org.junit.jupiter.api.Test;

class MovementPolicyTest {
    @Test
    void spectatorBypassesParity() {
        assertTrue(MovementPolicy.bypassParity(GameMode.SPECTATOR));
    }

    @Test
    void ordinaryGameModesStillUseParity() {
        assertFalse(MovementPolicy.bypassParity(GameMode.SURVIVAL));
        assertFalse(MovementPolicy.bypassParity(GameMode.CREATIVE));
        assertFalse(MovementPolicy.bypassParity(GameMode.ADVENTURE));
    }
}
