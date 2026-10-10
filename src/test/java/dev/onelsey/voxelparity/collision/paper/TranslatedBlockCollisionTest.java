package dev.onelsey.voxelparity.collision.paper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class TranslatedBlockCollisionTest {
    @Test
    void enabledKindsFollowModuleConfiguration() {
        TranslatedBlockCollision all = new TranslatedBlockCollision(true, true);
        assertEquals(
                Set.of(
                        TranslatedCollisionKind.BAMBOO,
                        TranslatedCollisionKind.POINTED_DRIPSTONE
                ),
                all.enabledKinds()
        );

        TranslatedBlockCollision bambooOnly = new TranslatedBlockCollision(true, false);
        assertTrue(bambooOnly.enabled(TranslatedCollisionKind.BAMBOO));
        assertFalse(bambooOnly.enabled(TranslatedCollisionKind.POINTED_DRIPSTONE));

        TranslatedBlockCollision none = new TranslatedBlockCollision(false, false);
        assertTrue(none.enabledKinds().isEmpty());
    }
}
