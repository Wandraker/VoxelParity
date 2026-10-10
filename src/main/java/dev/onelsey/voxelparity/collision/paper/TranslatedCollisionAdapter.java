package dev.onelsey.voxelparity.collision.paper;

import dev.onelsey.voxelparity.collision.math.Aabb;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.block.Block;

public interface TranslatedCollisionAdapter {
    TranslatedCollisionKind kind();

    Material material();

    Optional<Aabb> bedrockBox(Block block);
}
