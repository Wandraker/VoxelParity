package dev.onelsey.voxelparity.debug;

import dev.onelsey.voxelparity.collision.math.Aabb;
import dev.onelsey.voxelparity.collision.paper.PaperBoxes;
import dev.onelsey.voxelparity.util.BoxMath;
import dev.onelsey.voxelparity.util.WorldCollisionScanner;
import io.papermc.paper.event.player.PlayerFailMoveEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;

public final class MovementDebugProbe {
    private final JavaPlugin plugin;
    private final DebugRegistry debugRegistry;
    private static final long POSE_MOVE_ACCEPT_LOG_INTERVAL_NANOS = 250_000_000L;
    private static final long SWIMMING_CLIP_LOG_INTERVAL_NANOS = 500_000_000L;

    private final Map<UUID, Boolean> powderSnowPresence = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastPoseMoveAcceptLog = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastSwimmingClipLog = new ConcurrentHashMap<>();

    public MovementDebugProbe(JavaPlugin plugin, DebugRegistry debugRegistry) {
        this.plugin = plugin;
        this.debugRegistry = debugRegistry;
    }

    public void observeMove(Player player, Aabb target) {
        UUID uuid = player.getUniqueId();
        if (!debugRegistry.isEnabled(uuid)) {
            powderSnowPresence.remove(uuid);
            return;
        }

        BoundingBox targetBox = PaperBoxes.toPaper(target);
        WorldCollisionScanner.ScanResult scan = WorldCollisionScanner.scan(
                player.getWorld(),
                targetBox,
                Material.POWDER_SNOW
        );
        if (scan.unloadedChunkEncountered()) {
            return;
        }

        boolean inside = scan.specialBlockEncountered();
        Boolean previous = powderSnowPresence.put(uuid, inside);
        if (previous != null && previous == inside) {
            return;
        }

        String state;
        if (previous == null) {
            state = inside ? "INSIDE" : "OUTSIDE";
        } else {
            state = inside ? "ENTER" : "EXIT";
        }

        plugin.getLogger().info(
                "[debug-snow] player=" + player.getName()
                        + " state=" + state
                        + " targetBox=" + format(targetBox)
                        + " " + describePlayerState(player)
        );
    }

    public void logFailMove(PlayerFailMoveEvent event) {
        Player player = event.getPlayer();
        if (!debugRegistry.isEnabled(player.getUniqueId())) {
            return;
        }

        BoundingBox targetBox = BoxMath.playerBoxAt(player, event.getTo());
        WorldCollisionScanner.ScanResult powderSnow = WorldCollisionScanner.scan(
                player.getWorld(),
                targetBox,
                Material.POWDER_SNOW
        );

        plugin.getLogger().info(
                "[debug-failmove] player=" + player.getName()
                        + " reason=" + event.getFailReason()
                        + " allowed=" + event.isAllowed()
                        + " from=" + format(event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ())
                        + " to=" + format(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ())
                        + " targetPowderSnow=" + powderSnow.specialBlockEncountered()
                        + " targetBox=" + format(targetBox)
                        + " " + describePlayerState(player)
        );
    }

    public void logPoseChange(Player player, Pose newPose) {
        if (!debugRegistry.isEnabled(player.getUniqueId())) {
            return;
        }

        BoundingBox current = player.getBoundingBox();
        BoundingBox nearby = new BoundingBox(
                current.getMinX() - 0.5D,
                current.getMinY() - 0.5D,
                current.getMinZ() - 0.5D,
                current.getMaxX() + 0.5D,
                current.getMaxY() + 0.5D,
                current.getMaxZ() + 0.5D
        );

        boolean powderSnowNearby = WorldCollisionScanner.scan(
                player.getWorld(),
                nearby,
                Material.POWDER_SNOW
        ).specialBlockEncountered();
        boolean dripstoneNearby = WorldCollisionScanner.scan(
                player.getWorld(),
                nearby,
                Material.POINTED_DRIPSTONE
        ).specialBlockEncountered();

        plugin.getLogger().info(
                "[debug-pose] player=" + player.getName()
                        + " currentPose=" + player.getPose()
                        + " newPose=" + newPose
                        + " powderSnowNearby=" + powderSnowNearby
                        + " pointedDripstoneNearby=" + dripstoneNearby
                        + " " + describePlayerState(player)
        );
    }

    public boolean shouldLogPoseMoveAccept(Player player) {
        UUID uuid = player.getUniqueId();
        if (!debugRegistry.isEnabled(uuid)) {
            return false;
        }

        long now = System.nanoTime();
        Long previous = lastPoseMoveAcceptLog.get(uuid);
        if (previous != null && now - previous < POSE_MOVE_ACCEPT_LOG_INTERVAL_NANOS) {
            return false;
        }

        lastPoseMoveAcceptLog.put(uuid, now);
        return true;
    }

    public boolean shouldLogSwimmingClip(Player player) {
        UUID uuid = player.getUniqueId();
        if (!debugRegistry.isEnabled(uuid) || player.getPose() != Pose.SWIMMING) {
            return false;
        }

        long now = System.nanoTime();
        Long previous = lastSwimmingClipLog.get(uuid);
        if (previous != null && now - previous < SWIMMING_CLIP_LOG_INTERVAL_NANOS) {
            return false;
        }

        lastSwimmingClipLog.put(uuid, now);
        return true;
    }

    public String describePlayerState(Player player) {
        BoundingBox box = player.getBoundingBox();
        return "gameMode=" + player.getGameMode()
                + " pose=" + player.getPose()
                + " bbox=" + format(box)
                + " bboxSize=" + format(
                        box.getMaxX() - box.getMinX(),
                        box.getMaxY() - box.getMinY(),
                        box.getMaxZ() - box.getMinZ()
                )
                + " sneaking=" + player.isSneaking()
                + " swimming=" + player.isSwimming()
                + " onGround=" + player.isOnGround()
                + " leatherBoots=" + hasLeatherBoots(player);
    }

    public void clear(UUID uuid) {
        powderSnowPresence.remove(uuid);
        lastPoseMoveAcceptLog.remove(uuid);
        lastSwimmingClipLog.remove(uuid);
    }

    private static boolean hasLeatherBoots(Player player) {
        ItemStack boots = player.getInventory().getBoots();
        return boots != null && boots.getType() == Material.LEATHER_BOOTS;
    }

    private static String format(BoundingBox box) {
        return String.format(
                "[%.6f,%.6f,%.6f -> %.6f,%.6f,%.6f]",
                box.getMinX(),
                box.getMinY(),
                box.getMinZ(),
                box.getMaxX(),
                box.getMaxY(),
                box.getMaxZ()
        );
    }

    private static String format(double x, double y, double z) {
        return String.format("%.4f,%.4f,%.4f", x, y, z);
    }
}
