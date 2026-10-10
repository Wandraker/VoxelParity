package dev.onelsey.voxelparity.collision.paper;

import dev.onelsey.voxelparity.collision.bedrock.BedrockBambooShape;
import dev.onelsey.voxelparity.collision.bedrock.BedrockDripstoneShape;
import dev.onelsey.voxelparity.collision.math.Aabb;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bamboo;
import org.bukkit.block.data.type.Speleothem;

public final class TranslatedBlockCollision {
    private final boolean bambooEnabled;
    private final boolean pointedDripstoneEnabled;

    public TranslatedBlockCollision(boolean bambooEnabled, boolean pointedDripstoneEnabled) {
        this.bambooEnabled = bambooEnabled;
        this.pointedDripstoneEnabled = pointedDripstoneEnabled;
    }

    public Optional<Aabb> bedrockBox(Block block) {
        BlockData data = block.getBlockData();

        if (bambooEnabled && block.getType() == Material.BAMBOO) {
            if (!(data instanceof Bamboo bamboo)) {
                return Optional.empty();
            }

            return Optional.of(BedrockBambooShape.worldBox(
                    block.getX(),
                    block.getY(),
                    block.getZ(),
                    BedrockBambooShape.fromJavaAge(bamboo.getAge())
            ));
        }

        if (!pointedDripstoneEnabled || block.getType() != Material.POINTED_DRIPSTONE) {
            return Optional.empty();
        }

        if (!(data instanceof Speleothem speleothem)) {
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

    public boolean bambooEnabled() {
        return bambooEnabled;
    }

    public boolean pointedDripstoneEnabled() {
        return pointedDripstoneEnabled;
    }
}
