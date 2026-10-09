package dev.onelsey.voxelparity.bedrock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BedrockPlayerDetector {
    private final JavaPlugin owner;
    private final String providerName;
    private final Object apiInstance;
    private final Method detectionMethod;
    private final AtomicBoolean failureLogged = new AtomicBoolean();

    private BedrockPlayerDetector(
            JavaPlugin owner,
            String providerName,
            Object apiInstance,
            Method detectionMethod
    ) {
        this.owner = owner;
        this.providerName = providerName;
        this.apiInstance = apiInstance;
        this.detectionMethod = detectionMethod;
    }

    public static Optional<BedrockPlayerDetector> detect(JavaPlugin owner) {
        PluginManager plugins = owner.getServer().getPluginManager();

        Optional<BedrockPlayerDetector> floodgate = createFloodgate(owner, plugins.getPlugin("floodgate"));
        if (floodgate.isPresent()) {
            return floodgate;
        }

        return createGeyser(owner, plugins.getPlugin("Geyser-Spigot"));
    }

    public String providerName() {
        return providerName;
    }

    public boolean isBedrockPlayer(UUID uuid) {
        try {
            return Boolean.TRUE.equals(detectionMethod.invoke(apiInstance, uuid));
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException error) {
            if (failureLogged.compareAndSet(false, true)) {
                owner.getLogger().severe("Bedrock player detection failed through " + providerName + ": " + error.getMessage());
            }
            return false;
        }
    }

    private static Optional<BedrockPlayerDetector> createFloodgate(JavaPlugin owner, Plugin plugin) {
        if (plugin == null || !plugin.isEnabled()) {
            return Optional.empty();
        }

        try {
            ClassLoader loader = plugin.getClass().getClassLoader();
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi", true, loader);
            Method getInstance = apiClass.getMethod("getInstance");
            Object api = getInstance.invoke(null);
            Method isFloodgatePlayer = apiClass.getMethod("isFloodgatePlayer", UUID.class);
            return Optional.of(new BedrockPlayerDetector(owner, "Floodgate", api, isFloodgatePlayer));
        } catch (ReflectiveOperationException | LinkageError error) {
            owner.getLogger().warning("Floodgate is installed but its API could not be initialized: " + error.getMessage());
            return Optional.empty();
        }
    }

    private static Optional<BedrockPlayerDetector> createGeyser(JavaPlugin owner, Plugin plugin) {
        if (plugin == null || !plugin.isEnabled()) {
            return Optional.empty();
        }

        try {
            ClassLoader loader = plugin.getClass().getClassLoader();
            Class<?> apiClass = Class.forName("org.geysermc.geyser.api.GeyserApi", true, loader);
            Method apiMethod = apiClass.getMethod("api");
            Object api = apiMethod.invoke(null);
            Method isBedrockPlayer = apiClass.getMethod("isBedrockPlayer", UUID.class);
            return Optional.of(new BedrockPlayerDetector(owner, "Geyser", api, isBedrockPlayer));
        } catch (ReflectiveOperationException | LinkageError error) {
            owner.getLogger().warning("Geyser-Spigot is installed but its API could not be initialized: " + error.getMessage());
            return Optional.empty();
        }
    }
}
