package dev.onelsey.voxelparity.collision.paper;

import dev.onelsey.voxelparity.collision.math.Aabb;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        EnumSet<TranslatedCollisionKind> javaTranslatedKinds =
                EnumSet.noneOf(TranslatedCollisionKind.class);
        EnumSet<TranslatedCollisionKind> bedrockTranslatedKinds =
                EnumSet.noneOf(TranslatedCollisionKind.class);
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
                    Optional<TranslatedCollision> translation = translated.translation(block);
                    if (translation.isPresent()) {
                        TranslatedCollision collision = translation.get();
                        if (javaShapeOverlaps(block, target)) {
                            javaTranslatedKinds.add(collision.kind());
                        }
                        if (collision.box().overlaps(target)) {
                            bedrockTranslatedKinds.add(collision.kind());
                        }
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
                Set.copyOf(javaTranslatedKinds),
                Set.copyOf(bedrockTranslatedKinds),
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
        List<TranslatedCollision> collisions = new ArrayList<>();
        boolean unloadedChunk = false;

        BlockRange range = BlockRange.from(world, bounds);
        for (int x = range.minX(); x <= range.maxX(); x++) {
            for (int z = range.minZ(); z <= range.maxZ(); z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    unloadedChunk = true;
                    continue;
                }

                for (int y = range.minY(); y <= range.maxY(); y++) {
                    translated.translation(world.getBlockAt(x, y, z)).ifPresent(collisions::add);
                }
            }
        }

        return new CollisionBoxes(List.copyOf(collisions), unloadedChunk);
    }

    public static List<TranslatedCollisionDiagnostic> translatedDiagnostics(
            World world,
            Aabb bounds,
            Aabb target,
            TranslatedBlockCollision translated
    ) {
        List<TranslatedCollisionDiagnostic> diagnostics = new ArrayList<>();

        BlockRange range = BlockRange.from(world, bounds);
        for (int x = range.minX(); x <= range.maxX(); x++) {
            for (int z = range.minZ(); z <= range.maxZ(); z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }

                for (int y = range.minY(); y <= range.maxY(); y++) {
                    Block block = world.getBlockAt(x, y, z);
                    Optional<TranslatedCollision> translation = translated.translation(block);
                    if (translation.isEmpty()) {
                        continue;
                    }

                    TranslatedCollision collision = translation.get();
                    List<Aabb> javaBoxes = javaShapeBoxes(block);
                    boolean javaTargetOverlap = javaBoxes.stream().anyMatch(target::overlaps);
                    boolean bedrockTargetOverlap = collision.box().overlaps(target);

                    diagnostics.add(new TranslatedCollisionDiagnostic(
                            collision.kind(),
                            block.getType().name(),
                            block.getX(),
                            block.getY(),
                            block.getZ(),
                            block.getBlockData().getAsString(),
                            javaBoxes,
                            collision.box(),
                            javaTargetOverlap,
                            bedrockTargetOverlap
                    ));
                }
            }
        }

        return List.copyOf(diagnostics);
    }


    public static JavaCollisionDiagnostics javaCollisionDiagnostics(
            World world,
            Aabb swept,
            Aabb target,
            TranslatedBlockCollision translated
    ) {
        List<JavaCollisionDiagnostic> diagnostics = new ArrayList<>();
        boolean unloadedChunk = false;

        BlockRange range = BlockRange.from(world, swept);
        for (int x = range.minX(); x <= range.maxX(); x++) {
            for (int z = range.minZ(); z <= range.maxZ(); z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    unloadedChunk = true;
                    continue;
                }

                for (int y = range.minY(); y <= range.maxY(); y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!block.isCollidable() || translated.translation(block).isPresent()) {
                        continue;
                    }

                    List<Aabb> javaBoxes = javaShapeBoxes(block);
                    boolean targetOverlap = javaBoxes.stream().anyMatch(target::overlaps);
                    boolean sweptOverlap = javaBoxes.stream().anyMatch(swept::overlaps);
                    if (!targetOverlap && !sweptOverlap) {
                        continue;
                    }

                    diagnostics.add(new JavaCollisionDiagnostic(
                            block.getType().name(),
                            block.getX(),
                            block.getY(),
                            block.getZ(),
                            block.getBlockData().getAsString(),
                            javaBoxes,
                            targetOverlap,
                            sweptOverlap
                    ));
                }
            }
        }

        return new JavaCollisionDiagnostics(List.copyOf(diagnostics), unloadedChunk);
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

    private static List<Aabb> javaShapeBoxes(Block block) {
        List<Aabb> boxes = new ArrayList<>();
        for (BoundingBox box : block.getCollisionShape().getBoundingBoxes()) {
            boxes.add(PaperBoxes.move(
                    PaperBoxes.fromPaper(box),
                    block.getX(),
                    block.getY(),
                    block.getZ()
            ));
        }
        return List.copyOf(boxes);
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
            Set<TranslatedCollisionKind> javaTranslatedKinds,
            Set<TranslatedCollisionKind> bedrockTranslatedKinds,
            boolean otherBlockCollision,
            boolean entityCollision,
            boolean unloadedChunk
    ) {
        public boolean javaTranslatedOverlap() {
            return !javaTranslatedKinds.isEmpty();
        }

        public boolean bedrockTranslatedOverlap() {
            return !bedrockTranslatedKinds.isEmpty();
        }

        public Set<TranslatedCollisionKind> relevantKinds() {
            EnumSet<TranslatedCollisionKind> kinds =
                    EnumSet.noneOf(TranslatedCollisionKind.class);
            kinds.addAll(javaTranslatedKinds);
            kinds.addAll(bedrockTranslatedKinds);
            return Set.copyOf(kinds);
        }
    }

    public record CollisionBoxes(
            List<TranslatedCollision> collisions,
            boolean unloadedChunk
    ) {
        public List<Aabb> boxes() {
            return collisions.stream().map(TranslatedCollision::box).toList();
        }

        public Set<TranslatedCollisionKind> kindsOverlapping(Aabb target) {
            EnumSet<TranslatedCollisionKind> kinds =
                    EnumSet.noneOf(TranslatedCollisionKind.class);
            for (TranslatedCollision collision : collisions) {
                if (collision.box().overlaps(target)) {
                    kinds.add(collision.kind());
                }
            }
            return Set.copyOf(kinds);
        }
    }


    public record JavaCollisionDiagnostics(
            List<JavaCollisionDiagnostic> blocks,
            boolean unloadedChunkEncountered
    ) {
    }

    public record JavaCollisionDiagnostic(
            String material,
            int x,
            int y,
            int z,
            String blockData,
            List<Aabb> javaBoxes,
            boolean targetOverlap,
            boolean sweptOverlap
    ) {
    }

    public record TranslatedCollisionDiagnostic(
            TranslatedCollisionKind kind,
            String material,
            int x,
            int y,
            int z,
            String blockData,
            List<Aabb> javaBoxes,
            Aabb bedrockBox,
            boolean javaTargetOverlap,
            boolean bedrockTargetOverlap
    ) {
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
