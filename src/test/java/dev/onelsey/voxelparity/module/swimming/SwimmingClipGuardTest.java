package dev.onelsey.voxelparity.module.swimming;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SwimmingClipGuardTest {
    @Test
    void clearSweptVolumeCanBeAllowed() {
        assertTrue(SwimmingClipGuard.canAllow(false, false, false));
    }

    @Test
    void anySafetyHazardBlocksAllowance() {
        assertFalse(SwimmingClipGuard.canAllow(true, false, false));
        assertFalse(SwimmingClipGuard.canAllow(false, true, false));
        assertFalse(SwimmingClipGuard.canAllow(false, false, true));
    }
}
