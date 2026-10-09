package dev.onelsey.voxelparity.movement;

import org.bukkit.configuration.file.FileConfiguration;

public interface MovementModule {
    String id();

    boolean enabled();

    void reload(FileConfiguration config);

    ModuleEvaluation evaluate(FailMoveContext context);

    long allowedMoves();

    long blockedCandidates();
}
