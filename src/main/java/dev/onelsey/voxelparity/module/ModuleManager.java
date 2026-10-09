package dev.onelsey.voxelparity.module;

import dev.onelsey.voxelparity.collision.paper.CollisionParityService;
import dev.onelsey.voxelparity.movement.MovementModule;
import dev.onelsey.voxelparity.module.powdersnow.PowderSnowModule;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ModuleManager {
    private final List<MovementModule> movementModules;
    private final CollisionParityService collisionParityService = new CollisionParityService();

    public ModuleManager(JavaPlugin plugin) {
        movementModules = List.of(new PowderSnowModule(plugin));
    }

    public void reload(FileConfiguration config) {
        for (MovementModule module : movementModules) {
            module.reload(config);
        }
        collisionParityService.reload(config);
    }

    public List<MovementModule> modules() {
        return movementModules;
    }

    public CollisionParityService collisionParityService() {
        return collisionParityService;
    }

    public int enabledModuleCount() {
        int count = 0;
        for (MovementModule module : movementModules) {
            if (module.enabled()) {
                count++;
            }
        }
        if (collisionParityService.bambooEnabled()) {
            count++;
        }
        if (collisionParityService.pointedDripstoneEnabled()) {
            count++;
        }
        return count;
    }
}
