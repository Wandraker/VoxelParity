package dev.onelsey.voxelparity.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public final class BoxMath {
    private BoxMath() {
    }

    public static BoundingBox playerBoxAt(Player player, Location target) {
        BoundingBox current = player.getBoundingBox();
        Location location = player.getLocation();
        double dx = target.getX() - location.getX();
        double dy = target.getY() - location.getY();
        double dz = target.getZ() - location.getZ();
        return translate(current, dx, dy, dz);
    }

    public static BoundingBox translate(BoundingBox box, double dx, double dy, double dz) {
        return new BoundingBox(
                box.getMinX() + dx,
                box.getMinY() + dy,
                box.getMinZ() + dz,
                box.getMaxX() + dx,
                box.getMaxY() + dy,
                box.getMaxZ() + dz
        );
    }

    public static BoundingBox union(BoundingBox first, BoundingBox second) {
        return new BoundingBox(
                Math.min(first.getMinX(), second.getMinX()),
                Math.min(first.getMinY(), second.getMinY()),
                Math.min(first.getMinZ(), second.getMinZ()),
                Math.max(first.getMaxX(), second.getMaxX()),
                Math.max(first.getMaxY(), second.getMaxY()),
                Math.max(first.getMaxZ(), second.getMaxZ())
        );
    }

    public static boolean strictlyOverlaps(BoundingBox first, BoundingBox second, double epsilon) {
        return first.getMaxX() > second.getMinX() + epsilon
                && first.getMinX() < second.getMaxX() - epsilon
                && first.getMaxY() > second.getMinY() + epsilon
                && first.getMinY() < second.getMaxY() - epsilon
                && first.getMaxZ() > second.getMinZ() + epsilon
                && first.getMinZ() < second.getMaxZ() - epsilon;
    }
}
