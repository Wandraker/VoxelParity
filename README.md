# VoxelParity

VoxelParity is a Bedrock movement and collision parity plugin for modern Java Edition servers running through Geyser.

The project is Leaf-first and currently targets Minecraft/Paper API 26.3 with Java 25. Paper, Purpur and Folia-compatible behavior is kept in mind where the API permits it.

## Current state

Version: `0.4.1`

Implemented:

- Bedrock-only handling through Floodgate or Geyser detection.
- Powder-snow failed-movement correction.
- Conservative Bedrock swimming false-clip recovery when Leaf rejects a move even though the entire swept Java collision volume is clear.
- Bedrock spectator assist that only takes over after movement stalls, supports forward/back/strafe/up/down input, uses short bounded bursts and clears residual velocity after assisted movement.
- Per-player bamboo collision parity with Bedrock thin/thick stalk geometry selected from the Java bamboo age.
- Per-player pointed-dripstone collision parity with recent swimming/crawl pose transition handling.
- Adapter-based translated-collision registry for independently maintained parity modules.
- Accepted Bedrock movement is corrected only when the reported target position actually ends inside a translated Bedrock collision box, unless a recent server-observed swimming/crawl transition proves the compact player box is clear.
- Failed Java movement is bypassed only when Java collision overlaps but the translated Bedrock collision does not.
- Entity, unrelated block and unloaded-chunk safety gates.
- Deterministic geometry and movement unit tests.
- GitHub Actions build on Java 25.
- Per-player debug logging, translated-collision geometry traces, powder-snow presence transitions, pose diagnostics, ordinary Java collision traces for swimming failures and per-module runtime counters.
- Always-on bounded in-memory movement trace recorder for Bedrock players with asynchronous `/vp dump <player>` export.

VoxelParity does not globally replace Java block collision shapes. Java players and mobs retain normal Java physics. Spectator-mode players are hard-exempt from all parity movement handling. Swimming false-clip recovery uses an independent Java collision scan plus axis-resolved path validation. A nearby corner inside the broad swept union is not treated as a collision by itself, while target overlap and actual wall-crossing remain blocked. Brief `isSwimming()` state that arrives before the server pose switches to `SWIMMING` is evaluated with a compact 0.6-block box without forcing the player's pose.

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
/vp dump <player>
/vp reload
```

Permission: `voxelparity.admin`

## Collision model

Bamboo and pointed dripstone use Bedrock-specific world-space collision boxes. VoxelParity applies those boxes only to Bedrock players.

The translated collision layer works in both directions:

1. When Java rejects a Bedrock move as clipped into a translated block, VoxelParity allows it only when the target no longer overlaps the Bedrock collision box and no unrelated collision is present.
2. When Java accepts movement, VoxelParity corrects it only if the reported target player box itself overlaps a translated Bedrock collision box. During a short, local grace window after a real server-observed `SWIMMING` pose transition, the same target is also checked with a 0.6-block-high compact box. If that compact box is clear, the movement is left untouched so the server/Geyser pose transition can complete instead of fighting the collision correction. A straight segment between two legal packet positions is not treated as proof of collision.

Java `BAMBOO` with `age=0` uses the Bedrock thin stalk width of `2/16`, while `age=1` uses the thick stalk width of `3/16`. Both retain the deterministic Bedrock position offset. `BAMBOO_SAPLING` is intentionally not translated; live Bedrock testing confirmed the sapling remains pass-through for player movement.

## Spectator assist

Bedrock spectator movement remains separate from normal collision parity. The assist listens to server-side movement input and only activates after the player makes too little progress for a short configurable window. It can assist forward, backward, strafing, jump and sneak directions, including pitch-aware forward/back movement.

Each assist is a short bounded burst. After a plugin-assisted burst, VoxelParity observes several real client/server movement ticks before it is allowed to assist again, preventing its own teleports from being mistaken for another stall. Targets in unloaded chunks are never used, and residual velocity is cleared while assisting to reduce the acceleration spike observed after escaping a block.

## Movement trace recorder

VoxelParity keeps a small bounded ring buffer of interesting Bedrock movement events in memory. It does not record every ordinary movement packet and it performs no continuous disk I/O. By default it keeps at most 128 entries for each of at most 64 recently observed players.

`/vp dump <player>` snapshots the buffer and writes it asynchronously under `plugins/VoxelParity/dumps/`.

## Powder snow safety model

The powder-snow module only considers movement that the server has already rejected as `CLIPPED_INTO_BLOCK`. It requires powder snow in the swept player volume, loaded chunks, no unrelated collidable block and the configured leather-boots rule. Live 0.2.4 testing showed normal Bedrock powder-snow traversal without any failed-movement event, so VoxelParity correctly stayed out of the way.

## License

No license has been selected yet.
