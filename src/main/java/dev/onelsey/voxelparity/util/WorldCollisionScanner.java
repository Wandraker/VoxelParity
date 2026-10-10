package dev.onelsey.voxelparity.util;

import java.util.ArrayList;
import java.util.List;
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
        DetailedScan detailed = scanDetailed(world, sweptBox, specialMaterial);
        return new ScanResult(
                detailed.specialBlockEncountered(),
                detailed.scanLimitExceeded() || !detailed.collisionBoxes().isEmpty(),
                detailed.scanLimitExceeded() || detailed.unloadedChunkEncountered()
        );
    }

    public static DetailedScan scanDetailed(
            World world,
            BoundingBox sweptBox,
            Material specialMaterial
    ) {
        ScanRange range = ScanRange.from(world, sweptBox);
        if (range.volume() <= 0L || range.volume() > MAX_SCANNED_BLOCKS) {
            return new DetailedScan(false, false, true, List.of());
        }

        boolean specialBlockEncountered = false;
        boolean unloadedChunkEncountered = false;
        List<BoundingBox> collisionBoxes = new ArrayList<>();

        for (int x = range.minX(); x <= range.maxX(); x++) {
            for (int z = range.minZ(); z <= range.maxZ(); z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    unloadedChunkEncountered = true;
                    continue;
                }

                for (int y = range.minY(); y <= range.maxY(); y++) {
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
                            collisionBoxes.add(worldBox);
                        }
                    }
                }
            }
        }

        return new DetailedScan(
                specialBlockEncountered,
                unloadedChunkEncountered,
                false,
                List.copyOf(collisionBoxes)
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

    public record DetailedScan(
            boolean specialBlockEncountered,
            boolean unloadedChunkEncountered,
            boolean scanLimitExceeded,
            List<BoundingBox> collisionBoxes
    ) {
    }

    private record ScanRange(
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ
    ) {
        private static ScanRange from(World world, BoundingBox box) {
            return new ScanRange(
                    floor(box.getMinX()),
                    Math.max(world.getMinHeight(), floor(box.getMinY())),
                    floor(box.getMinZ()),
                    floor(Math.nextDown(box.getMaxX())),
                    Math.min(world.getMaxHeight() - 1, floor(Math.nextDown(box.getMaxY()))),
                    floor(Math.nextDown(box.getMaxZ()))
            );
        }

        private long volume() {
            return (long) (maxX - minX + 1)
                    * (long) (maxY - minY + 1)
                    * (long) (maxZ - minZ + 1);
        }
    }
}
