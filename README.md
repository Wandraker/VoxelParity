# VoxelParity

VoxelParity is a Bedrock movement and collision parity plugin for modern Java Edition servers running through Geyser.

The project is Leaf-first and currently targets Minecraft/Paper API 26.3 with Java 25. Paper, Purpur and Folia-compatible behavior is kept in mind where the API permits it.

## Current state

Version: `0.2.5-SNAPSHOT`

Implemented:

- Bedrock-only handling through Floodgate or Geyser detection.
- Powder-snow failed-movement correction.
- Per-player bamboo collision parity with Bedrock thin/thick stalk geometry selected from the Java bamboo age.
- Per-player pointed-dripstone collision parity with recent swimming/crawl pose transition handling.
- Shared translated-collision resolver.
- Accepted Bedrock movement is corrected only when the reported target position actually ends inside a translated Bedrock collision box, unless a recent server-observed swimming/crawl transition proves the compact player box is clear.
- Failed Java movement is bypassed only when Java collision overlaps but the translated Bedrock collision does not.
- Entity, unrelated block and unloaded-chunk safety gates.
- Deterministic geometry and movement unit tests.
- GitHub Actions build on Java 25.
- Per-player debug logging, translated-collision geometry traces, powder-snow presence transitions, pose diagnostics and runtime counters.

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
2. When Java accepts movement, VoxelParity corrects it only if the reported target player box itself overlaps a translated Bedrock collision box. During a short, local grace window after a real server-observed `SWIMMING` pose transition, the same target is also checked with a 0.6-block-high compact box. If that compact box is clear, the movement is left untouched so the server/Geyser pose transition can complete instead of fighting the collision correction. A straight segment between two legal packet positions is not treated as proof of collision.

Java `BAMBOO` with `age=0` uses the Bedrock thin stalk width of `2/16`, while `age=1` uses the thick stalk width of `3/16`. Both retain the deterministic Bedrock position offset. `BAMBOO_SAPLING` is intentionally not translated; live Bedrock testing confirmed the sapling remains pass-through for player movement.

## Powder snow safety model

The powder-snow module only considers movement that the server has already rejected as `CLIPPED_INTO_BLOCK`. It requires powder snow in the swept player volume, loaded chunks, no unrelated collidable block and the configured leather-boots rule. Live 0.2.4 testing showed normal Bedrock powder-snow traversal without any failed-movement event, so VoxelParity correctly stayed out of the way.

## License

No license has been selected yet.
