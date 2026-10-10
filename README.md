# VoxelParity

VoxelParity is a Bedrock movement and collision parity plugin for modern Java Edition servers running through Geyser.

The project is Leaf-first and currently targets Minecraft/Paper API 26.3 with Java 25. Paper, Purpur and Folia-compatible behavior is kept in mind where the API permits it.

## Current state

Version: `0.2.2-SNAPSHOT`

Implemented:

- Bedrock-only handling through Floodgate or Geyser detection.
- Powder-snow failed-movement correction.
- Per-player bamboo collision parity with Bedrock thin/thick stalk geometry selected from the Java bamboo age.
- Per-player pointed-dripstone collision parity.
- Shared translated-collision resolver.
- Normal movement clipping against Bedrock collision boxes to prevent pass-through exploits.
- Failed Java movement is bypassed only when Java collision overlaps but the translated Bedrock collision does not.
- Entity, unrelated block and unloaded-chunk safety gates.
- Deterministic geometry and movement unit tests.
- GitHub Actions build on Java 25.
- Per-player debug logging, translated-collision geometry traces and runtime counters.

VoxelParity does not globally replace Java block collision shapes. Java players and mobs retain normal Java physics.

## Requirements

- Java 25
- Leaf/Paper-compatible server on 26.3
- Geyser-Spigot or Floodgate available on the same server instance

If Geyser runs on a proxy or another machine, Floodgate must be available on the backend for VoxelParity to identify Bedrock players.

## Build

```text
mvn clean verify
```

The plugin jar is created under `target/`.

## Commands

```text
/vp status
/vp modules
/vp debug <player> [on|off]
/vp reload
```

Permission: `voxelparity.admin`

## Collision model

Bamboo and pointed dripstone use Bedrock-specific world-space collision boxes. VoxelParity applies those boxes only to Bedrock players.

The translated collision layer works in both directions:

1. When Java rejects a Bedrock move as clipped into a translated block, VoxelParity allows it only when the target no longer overlaps the Bedrock collision box and no unrelated collision is present.
2. When Java accepts movement, VoxelParity clips the move against the Bedrock collision box so the parity fix cannot become a noclip path.

Java `BAMBOO` with `age=0` uses the Bedrock thin stalk width of `2/16`, while `age=1` uses the thick stalk width of `3/16`. Both retain the deterministic Bedrock position offset. `BAMBOO_SAPLING` is intentionally not translated and remains a separate case.

## Powder snow safety model

The powder-snow module only considers movement that the server has already rejected as `CLIPPED_INTO_BLOCK`. It requires powder snow in the swept player volume, loaded chunks, no unrelated collidable block and the configured leather-boots rule.

## License

No license has been selected yet.
