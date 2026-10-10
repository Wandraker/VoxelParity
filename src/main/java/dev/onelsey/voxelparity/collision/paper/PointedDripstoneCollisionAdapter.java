package dev.onelsey.voxelparity.collision.paper;

import dev.onelsey.voxelparity.collision.bedrock.BedrockDripstoneShape;
import dev.onelsey.voxelparity.collision.math.Aabb;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Speleothem;

public final class PointedDripstoneCollisionAdapter implements TranslatedCollisionAdapter {
    @Override
    public TranslatedCollisionKind kind() {
        return TranslatedCollisionKind.POINTED_DRIPSTONE;
    }

    @Override
    public Material material() {
        return Material.POINTED_DRIPSTONE;
    }

    @Override
    public Optional<Aabb> bedrockBox(Block block) {
        if (block.getType() != Material.POINTED_DRIPSTONE
                || !(block.getBlockData() instanceof Speleothem speleothem)) {
            return Optional.empty();
        }

        BedrockDripstoneShape.Thickness thickness = switch (speleothem.getThickness()) {
            case TIP -> BedrockDripstoneShape.Thickness.TIP;
            case TIP_MERGE -> BedrockDripstoneShape.Thickness.TIP_MERGE;
            case FRUSTUM -> BedrockDripstoneShape.Thickness.FRUSTUM;
            case MIDDLE -> BedrockDripstoneShape.Thickness.MIDDLE;
            case BASE -> BedrockDripstoneShape.Thickness.BASE;
        };

        boolean hanging = speleothem.getVerticalDirection() == BlockFace.DOWN;
        return Optional.of(BedrockDripstoneShape.worldBox(
                block.getX(),
                block.getY(),
                block.getZ(),
                thickness,
                hanging
        ));
    }
}
