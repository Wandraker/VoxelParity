package dev.onelsey.voxelparity;

import dev.onelsey.voxelparity.bedrock.BedrockPlayerDetector;
import dev.onelsey.voxelparity.command.VoxelParityCommand;
import dev.onelsey.voxelparity.debug.DebugRegistry;
import dev.onelsey.voxelparity.debug.MovementTraceRecorder;
import dev.onelsey.voxelparity.movement.MovementListener;
import dev.onelsey.voxelparity.module.ModuleManager;
import dev.onelsey.voxelparity.spectator.SpectatorAssistService;
import java.util.Optional;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class VoxelParityPlugin extends JavaPlugin {
    private BedrockPlayerDetector bedrockPlayerDetector;
    private DebugRegistry debugRegistry;
    private MovementTraceRecorder traceRecorder;
    private ModuleManager moduleManager;
    private SpectatorAssistService spectatorAssist;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Optional<BedrockPlayerDetector> detected = BedrockPlayerDetector.detect(this);
        if (detected.isEmpty()) {
            getLogger().severe("VoxelParity requires Floodgate or Geyser-Spigot on this server to identify Bedrock players.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        bedrockPlayerDetector = detected.get();
        debugRegistry = new DebugRegistry();
        traceRecorder = new MovementTraceRecorder(this);
        moduleManager = new ModuleManager(this);
        spectatorAssist = new SpectatorAssistService(
                this,
                bedrockPlayerDetector,
                traceRecorder
        );
        reloadVoxelParityConfiguration();

        getServer().getPluginManager().registerEvents(
                new MovementListener(
                        this,
                        bedrockPlayerDetector,
                        debugRegistry,
                        moduleManager,
                        traceRecorder
                ),
                this
        );
        getServer().getPluginManager().registerEvents(spectatorAssist, this);

        PluginCommand command = getCommand("voxelparity");
        if (command == null) {
            getLogger().severe("Command registration failed.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        VoxelParityCommand commandHandler = new VoxelParityCommand(
                this,
                bedrockPlayerDetector,
                debugRegistry,
                moduleManager,
                traceRecorder,
                spectatorAssist
        );
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);

        int activeModules = moduleManager.enabledModuleCount()
                + (spectatorAssist.enabled() ? 1 : 0);
        getLogger().info("Bedrock detector: " + bedrockPlayerDetector.providerName());
        getLogger().info("VoxelParity enabled with " + activeModules + " active module(s).");
    }

    public void reloadVoxelParityConfiguration() {
        reloadConfig();
        debugRegistry.setLogAll(getConfig().getBoolean("debug.log-decisions", false));
        traceRecorder.reload(getConfig());
        moduleManager.reload(getConfig());
        spectatorAssist.reload(getConfig());
    }
}
