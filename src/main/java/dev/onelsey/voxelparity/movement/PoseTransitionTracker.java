package dev.onelsey.voxelparity.movement;

import dev.onelsey.voxelparity.collision.math.Aabb;
import dev.onelsey.voxelparity.collision.paper.PaperBoxes;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

public final class PoseTransitionTracker {
    private static final long SWIMMING_GRACE_NANOS = 1_500_000_000L;
    private static final double MAX_ANCHOR_DISTANCE_SQUARED = 9.0D;
    private static final double SWIMMING_HEIGHT = 0.6D;

    private final Map<UUID, State> states = new ConcurrentHashMap<>();

    public void observePoseChange(Player player, Pose newPose) {
        Pose currentPose = player.getPose();
        if (currentPose != Pose.SWIMMING && newPose != Pose.SWIMMING) {
            return;
        }

        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        states.put(
                player.getUniqueId(),
                new State(
                        world.getUID(),
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        System.nanoTime() + SWIMMING_GRACE_NANOS
                )
        );
    }

    public Optional<Aabb> compactTarget(Player player, Location target) {
        if (player.getPose() == Pose.SWIMMING) {
            return Optional.empty();
        }

        State state = states.get(player.getUniqueId());
        if (state == null) {
            return Optional.empty();
        }

        long now = System.nanoTime();
        if (now > state.expiresAtNanos()) {
            states.remove(player.getUniqueId(), state);
            return Optional.empty();
        }

        World world = target.getWorld();
        if (world == null || !world.getUID().equals(state.worldId())) {
            return Optional.empty();
        }

        double dx = target.getX() - state.x();
        double dy = target.getY() - state.y();
        double dz = target.getZ() - state.z();
        if (dx * dx + dy * dy + dz * dz > MAX_ANCHOR_DISTANCE_SQUARED) {
            return Optional.empty();
        }

        Aabb normal = PaperBoxes.playerAt(player, target);
        if (normal.maxY() - normal.minY() <= SWIMMING_HEIGHT + 1.0E-7D) {
            return Optional.empty();
        }

        return Optional.of(new Aabb(
                normal.minX(),
                normal.minY(),
                normal.minZ(),
                normal.maxX(),
                normal.minY() + SWIMMING_HEIGHT,
                normal.maxZ()
        ));
    }

    public void clear(UUID uuid) {
        states.remove(uuid);
    }

    private record State(
            UUID worldId,
            double x,
            double y,
            double z,
            long expiresAtNanos
    ) {
    }
}
