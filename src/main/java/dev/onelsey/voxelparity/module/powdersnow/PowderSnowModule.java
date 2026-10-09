package dev.onelsey.voxelparity.module.powdersnow;

import dev.onelsey.voxelparity.movement.FailMoveContext;
import dev.onelsey.voxelparity.movement.ModuleEvaluation;
import dev.onelsey.voxelparity.movement.MovementModule;
import dev.onelsey.voxelparity.util.WorldCollisionScanner;
import java.util.concurrent.atomic.LongAdder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class PowderSnowModule implements MovementModule {
    private static final String ID = "powder-snow";

    private final JavaPlugin plugin;
    private final LongAdder allowedMoves = new LongAdder();
    private final LongAdder blockedCandidates = new LongAdder();

    private volatile boolean enabled;
    private volatile double maxMovementPerAxis;
    private volatile boolean requireNoLeatherBoots;
    private volatile boolean suppressVanillaWarning;

    public PowderSnowModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public void reload(FileConfiguration config) {
        enabled = config.getBoolean("modules.powder-snow.enabled", true);
        maxMovementPerAxis = Math.max(0.1, config.getDouble("modules.powder-snow.max-movement-per-axis", 8.0));
        requireNoLeatherBoots = config.getBoolean("modules.powder-snow.require-no-leather-boots", true);
        suppressVanillaWarning = config.getBoolean("modules.powder-snow.suppress-vanilla-warning", true);
    }

    @Override
    public ModuleEvaluation evaluate(FailMoveContext context) {
        double dx = context.to().getX() - context.from().getX();
        double dy = context.to().getY() - context.from().getY();
        double dz = context.to().getZ() - context.from().getZ();
        if (Math.abs(dx) > maxMovementPerAxis
                || Math.abs(dy) > maxMovementPerAxis
                || Math.abs(dz) > maxMovementPerAxis) {
            return ModuleEvaluation.abstain(ID, "movement-exceeds-module-limit");
        }

        if (requireNoLeatherBoots && hasLeatherBoots(context)) {
            return ModuleEvaluation.abstain(ID, "leather-boots-preserve-java-collision");
        }

        WorldCollisionScanner.ScanResult scan = WorldCollisionScanner.scan(
                context.player().getWorld(),
                context.sweptBox(),
                Material.POWDER_SNOW
        );

        if (!scan.specialBlockEncountered()) {
            return ModuleEvaluation.abstain(ID, "no-powder-snow-in-swept-volume");
        }

        if (scan.unloadedChunkEncountered()) {
            blockedCandidates.increment();
            return ModuleEvaluation.blocked(ID, "unloaded-chunk-in-swept-volume");
        }

        if (scan.otherCollisionEncountered()) {
            blockedCandidates.increment();
            return ModuleEvaluation.blocked(ID, "non-powder-snow-collision-in-swept-volume");
        }

        allowedMoves.increment();
        if (plugin.isEnabled()) {
            return ModuleEvaluation.allow(ID, "powder-snow-only-collision-mismatch", suppressVanillaWarning);
        }
        return ModuleEvaluation.abstain(ID, "plugin-disabled");
    }

    @Override
    public long allowedMoves() {
        return allowedMoves.sum();
    }

    @Override
    public long blockedCandidates() {
        return blockedCandidates.sum();
    }

    private static boolean hasLeatherBoots(FailMoveContext context) {
        ItemStack boots = context.player().getInventory().getBoots();
        return boots != null && boots.getType() == Material.LEATHER_BOOTS;
    }
}
