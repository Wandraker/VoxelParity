package dev.onelsey.voxelparity.debug;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DebugRegistry {
    private final Set<UUID> players = ConcurrentHashMap.newKeySet();
    private volatile boolean logAll;

    public boolean isEnabled(UUID uuid) {
        return logAll || players.contains(uuid);
    }

    public boolean isPlayerEnabled(UUID uuid) {
        return players.contains(uuid);
    }

    public void setPlayerEnabled(UUID uuid, boolean enabled) {
        if (enabled) {
            players.add(uuid);
        } else {
            players.remove(uuid);
        }
    }

    public void setLogAll(boolean logAll) {
        this.logAll = logAll;
    }

    public boolean logAll() {
        return logAll;
    }
}
