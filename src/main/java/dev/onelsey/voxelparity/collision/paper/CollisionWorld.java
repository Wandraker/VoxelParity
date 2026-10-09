package dev.onelsey.voxelparity.collision.paper;

import dev.onelsey.voxelparity.collision.math.Aabb;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.BoundingBox;

public final class CollisionWorld {
    private CollisionWorld() {
    }

    public static TargetScan scanTarget(
            Player player,
            Aabb target,
            TranslatedBlockCollision translated
    ) {
        World world = player.getWorld();
        boolean javaTranslatedOverlap = false;
        boolean bedrockTranslatedOverlap = false;
        boolean otherBlockCollision = false;
        boolean unloadedChunk = false;

        BlockRange range = BlockRange.from(world, target);
        for (int x = range.minX(); x <= range.maxX(); x++) {
            for (int z = range.minZ(); z <= range.maxZ(); z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    unloadedChunk = true;
                    continue;
                }

                for (int y = range.minY(); y <= range.maxY(); y++) {
                    Block block = world.getBlockAt(x, y, z);
                    Optional<Aabb> bedrock = translated.bedrockBox(block);
                    if (bedrock.isPresent()) {
                        javaTranslatedOverlap |= javaShapeOverlaps(block, target);
                        bedrockTranslatedOverlap |= bedrock.get().overlaps(target);
                    } else if (block.isCollidable() && javaShapeOverlaps(block, target)) {
                        otherBlockCollision = true;
                    }
                }
            }
        }

        boolean entityCollision = !world.getNearbyEntities(
                PaperBoxes.toPaper(target),
                entity -> collidesWithPlayer(entity, player)
        ).isEmpty();

        return new TargetScan(
                javaTranslatedOverlap,
                bedrockTranslatedOverlap,
                otherBlockCollision,
                entityCollision,
                unloadedChunk
        );
    }

    public static CollisionBoxes translatedBoxes(
            World world,
            Aabb bounds,
            TranslatedBlockCollision translated
    ) {
        List<Aabb> boxes = new ArrayList<>();
        boolean unloadedChunk = false;

        BlockRange range = BlockRange.from(world, bounds);
        for (int x = range.minX(); x <= range.maxX(); x++) {
            for (int z = range.minZ(); z <= range.maxZ(); z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    unloadedChunk = true;
                    continue;
                }

                for (int y = range.minY(); y <= range.maxY(); y++) {
                    translated.bedrockBox(world.getBlockAt(x, y, z)).ifPresent(boxes::add);
                }
            }
        }

        return new CollisionBoxes(List.copyOf(boxes), unloadedChunk);
    }

    private static boolean javaShapeOverlaps(Block block, Aabb target) {
        Collection<BoundingBox> local = block.getCollisionShape().getBoundingBoxes();
        for (BoundingBox box : local) {
            Aabb worldBox = PaperBoxes.move(
                    PaperBoxes.fromPaper(box),
                    block.getX(),
                    block.getY(),
                    block.getZ()
            );
            if (worldBox.overlaps(target)) {
                return true;
            }
        }
        return false;
    }

    private static boolean collidesWithPlayer(Entity entity, Player player) {
        if (entity == player) {
            return false;
        }
        if (entity instanceof LivingEntity living) {
            boolean exempt = living.getCollidableExemptions().contains(player.getUniqueId());
            return living.isCollidable() != exempt;
        }
        return entity instanceof Vehicle || entity instanceof EnderCrystal;
    }

    public record TargetScan(
            boolean javaTranslatedOverlap,
            boolean bedrockTranslatedOverlap,
            boolean otherBlockCollision,
            boolean entityCollision,
            boolean unloadedChunk
    ) {
    }

    public record CollisionBoxes(List<Aabb> boxes, boolean unloadedChunk) {
    }

    private record BlockRange(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        private static BlockRange from(World world, Aabb box) {
            int minX = floor(box.minX());
            int minY = Math.max(world.getMinHeight(), floor(box.minY()));
            int minZ = floor(box.minZ());
            int maxX = floor(Math.nextDown(box.maxX()));
            int maxY = Math.min(world.getMaxHeight() - 1, floor(Math.nextDown(box.maxY())));
            int maxZ = floor(Math.nextDown(box.maxZ()));
            return new BlockRange(minX, minY, minZ, maxX, maxY, maxZ);
        }

        private static int floor(double value) {
            return (int) Math.floor(value);
        }
    }
}
