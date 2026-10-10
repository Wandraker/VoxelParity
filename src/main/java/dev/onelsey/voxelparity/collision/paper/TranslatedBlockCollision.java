package dev.onelsey.voxelparity.collision.paper;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class TranslatedBlockCollision {
    private final Map<Material, TranslatedCollisionAdapter> adapters =
            new EnumMap<>(Material.class);
    private final EnumSet<TranslatedCollisionKind> enabledKinds =
            EnumSet.noneOf(TranslatedCollisionKind.class);

    public TranslatedBlockCollision(boolean bambooEnabled, boolean pointedDripstoneEnabled) {
        register(new BambooCollisionAdapter(), bambooEnabled);
        register(new PointedDripstoneCollisionAdapter(), pointedDripstoneEnabled);
    }

    public Optional<TranslatedCollision> translation(Block block) {
        TranslatedCollisionAdapter adapter = adapters.get(block.getType());
        if (adapter == null) {
            return Optional.empty();
        }

        return adapter.bedrockBox(block)
                .map(box -> new TranslatedCollision(adapter.kind(), box));
    }

    public boolean enabled(TranslatedCollisionKind kind) {
        return enabledKinds.contains(kind);
    }

    public Set<TranslatedCollisionKind> enabledKinds() {
        return Set.copyOf(enabledKinds);
    }

    private void register(TranslatedCollisionAdapter adapter, boolean enabled) {
        if (!enabled) {
            return;
        }

        TranslatedCollisionAdapter previous = adapters.put(adapter.material(), adapter);
        if (previous != null) {
            throw new IllegalStateException(
                    "Duplicate translated collision adapter for " + adapter.material()
            );
        }

        enabledKinds.add(adapter.kind());
    }
}
