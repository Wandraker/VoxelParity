package dev.onelsey.voxelparity.collision.paper;

import dev.onelsey.voxelparity.collision.bedrock.BedrockBambooShape;
import dev.onelsey.voxelparity.collision.math.Aabb;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bamboo;

public final class BambooCollisionAdapter implements TranslatedCollisionAdapter {
    @Override
    public TranslatedCollisionKind kind() {
        return TranslatedCollisionKind.BAMBOO;
    }

    @Override
    public Material material() {
        return Material.BAMBOO;
    }

    @Override
    public Optional<Aabb> bedrockBox(Block block) {
        if (block.getType() != Material.BAMBOO
                || !(block.getBlockData() instanceof Bamboo bamboo)) {
            return Optional.empty();
        }

        return Optional.of(BedrockBambooShape.worldBox(
                block.getX(),
                block.getY(),
                block.getZ(),
                BedrockBambooShape.fromJavaAge(bamboo.getAge())
        ));
    }
}
