# VoxelParity

VoxelParity is a Bedrock movement and collision parity plugin for modern Java Edition servers running through Geyser.

The project is Leaf-first and targets Minecraft/Paper API 26.3 with Java 25. Paper, Purpur and Folia-compatible behavior is kept in mind where the API permits it.

## Current state

Version: `0.1.0-SNAPSHOT`

Implemented:

- Bedrock-only movement correction through Floodgate or Geyser detection.
- `powder-snow` module for `PlayerFailMoveEvent.CLIPPED_INTO_BLOCK` rubberband cases.
- Conservative swept-volume collision validation before a rejected move can be allowed.
- Leather boots preserve vanilla Java-side powder-snow collision behavior by default.
- No chunk loads during validation.
- Per-player debug logging.
- Runtime counters for allowed and blocked candidate moves.

Planned next:

- Bedrock bamboo collision parity.
- Bedrock pointed dripstone collision parity.
- Shared collision resolver for modules that need Bedrock-specific collision boxes.
- More movement parity fixes backed by reproducible tests.

## Requirements

- Java 25
- Leaf/Paper-compatible server on 26.3
- Geyser-Spigot or Floodgate available on the same server instance

If Geyser runs on a proxy or another machine, Floodgate must be available on the backend for VoxelParity to identify Bedrock players.

## Build

```text
mvn clean package
```

The plugin jar will be created under `target/`.

## Commands

```text
/vp status
/vp modules
/vp debug <player> [on|off]
/vp reload
```

Permission: `voxelparity.admin`

## Powder snow safety model

VoxelParity does not teleport a player every tick and does not globally alter block collision.

The powder-snow module only considers movement that the server has already rejected as `CLIPPED_INTO_BLOCK`. It then requires all of the following before bypassing that rejection:

- the player is identified as Bedrock;
- powder snow occurs in the swept player volume;
- the movement stays under the configured per-axis limit;
- no unloaded chunk is touched;
- no other collidable block overlaps the swept player volume;
- leather boots are not equipped when the default safety option is enabled.

Java players are never handled by the module.

## License

No license has been selected yet.
