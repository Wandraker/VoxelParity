package dev.onelsey.voxelparity.collision.paper;

import dev.onelsey.voxelparity.collision.math.Aabb;
import java.util.Objects;

public record TranslatedCollision(
        TranslatedCollisionKind kind,
        Aabb box
) {
    public TranslatedCollision {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(box, "box");
    }
}
