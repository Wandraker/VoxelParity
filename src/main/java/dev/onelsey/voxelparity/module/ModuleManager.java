package dev.onelsey.voxelparity.module;

import dev.onelsey.voxelparity.movement.MovementModule;
import dev.onelsey.voxelparity.module.powdersnow.PowderSnowModule;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ModuleManager {
    private final List<MovementModule> modules;

    public ModuleManager(JavaPlugin plugin) {
        modules = List.of(new PowderSnowModule(plugin));
    }

    public void reload(FileConfiguration config) {
        for (MovementModule module : modules) {
            module.reload(config);
        }
    }

    public List<MovementModule> modules() {
        return modules;
    }

    public int enabledModuleCount() {
        int count = 0;
        for (MovementModule module : modules) {
            if (module.enabled()) {
                count++;
            }
        }
        return count;
    }
}
