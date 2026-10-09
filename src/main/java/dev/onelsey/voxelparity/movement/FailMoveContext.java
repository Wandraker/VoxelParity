package dev.onelsey.voxelparity.movement;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public record FailMoveContext(
        Player player,
        Location from,
        Location to,
        BoundingBox fromBox,
        BoundingBox toBox,
        BoundingBox sweptBox
) {
}
