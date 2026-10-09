package dev.onelsey.voxelparity.util;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

public final class WorldCollisionScanner {
    private static final double EPSILON = 1.0E-7D;
    private static final int MAX_SCANNED_BLOCKS = 4096;

    private WorldCollisionScanner() {
    }

    public static ScanResult scan(World world, BoundingBox sweptBox, Material specialMaterial) {
        int minX = floor(sweptBox.getMinX());
        int minY = Math.max(world.getMinHeight(), floor(sweptBox.getMinY()));
        int minZ = floor(sweptBox.getMinZ());
        int maxX = floor(Math.nextDown(sweptBox.getMaxX()));
        int maxY = Math.min(world.getMaxHeight() - 1, floor(Math.nextDown(sweptBox.getMaxY())));
        int maxZ = floor(Math.nextDown(sweptBox.getMaxZ()));

        long volume = (long) (maxX - minX + 1)
                * (long) (maxY - minY + 1)
                * (long) (maxZ - minZ + 1);
        if (volume <= 0L || volume > MAX_SCANNED_BLOCKS) {
            return new ScanResult(false, true, true);
        }

        boolean specialBlockEncountered = false;
        boolean otherCollisionEncountered = false;
        boolean unloadedChunkEncountered = false;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    unloadedChunkEncountered = true;
                    continue;
                }

                for (int y = minY; y <= maxY; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == specialMaterial) {
                        specialBlockEncountered = true;
                        continue;
                    }

                    if (!block.isCollidable()) {
                        continue;
                    }

                    for (BoundingBox localBox : block.getCollisionShape().getBoundingBoxes()) {
                        BoundingBox worldBox = BoxMath.translate(localBox, x, y, z);
                        if (BoxMath.strictlyOverlaps(worldBox, sweptBox, EPSILON)) {
                            otherCollisionEncountered = true;
                            break;
                        }
                    }
                }
            }
        }

        return new ScanResult(
                specialBlockEncountered,
                otherCollisionEncountered,
                unloadedChunkEncountered
        );
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }

    public record ScanResult(
            boolean specialBlockEncountered,
            boolean otherCollisionEncountered,
            boolean unloadedChunkEncountered
    ) {
    }
}
