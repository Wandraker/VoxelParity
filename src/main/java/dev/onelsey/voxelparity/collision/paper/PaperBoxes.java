package dev.onelsey.voxelparity.collision.paper;

import dev.onelsey.voxelparity.collision.math.Aabb;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public final class PaperBoxes {
    private PaperBoxes() {
    }

    public static Aabb playerAt(Player player, Location target) {
        BoundingBox current = player.getBoundingBox();
        Location position = player.getLocation();
        double dx = target.getX() - position.getX();
        double dy = target.getY() - position.getY();
        double dz = target.getZ() - position.getZ();
        return new Aabb(
                current.getMinX() + dx,
                current.getMinY() + dy,
                current.getMinZ() + dz,
                current.getMaxX() + dx,
                current.getMaxY() + dy,
                current.getMaxZ() + dz
        );
    }

    public static Aabb fromPaper(BoundingBox box) {
        return new Aabb(
                box.getMinX(),
                box.getMinY(),
                box.getMinZ(),
                box.getMaxX(),
                box.getMaxY(),
                box.getMaxZ()
        );
    }

    public static BoundingBox toPaper(Aabb box) {
        return new BoundingBox(
                box.minX(),
                box.minY(),
                box.minZ(),
                box.maxX(),
                box.maxY(),
                box.maxZ()
        );
    }

    public static Aabb move(Aabb box, double x, double y, double z) {
        return new Aabb(
                box.minX() + x,
                box.minY() + y,
                box.minZ() + z,
                box.maxX() + x,
                box.maxY() + y,
                box.maxZ() + z
        );
    }
}
