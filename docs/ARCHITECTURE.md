# Architecture

## Goal

VoxelParity corrects specific Bedrock-to-Java movement and collision mismatches without globally changing Java server physics.

## Invariants

- Java players are never modified by a Bedrock parity module.
- A module must be narrowly scoped to a reproducible edition mismatch.
- VoxelParity does not globally replace vanilla collision shapes.
- VoxelParity does not load chunks while validating movement.
- A rejected movement packet is never allowed when an unrelated solid collision is present in the guarded volume.
- Normal server authority remains the default. A module must positively prove that its own mismatch is relevant before bypassing a rejection.

## Layers

### Bedrock detection

`BedrockPlayerDetector` discovers Floodgate first and Geyser-Spigot second. The integration is reflective so VoxelParity does not need to package or hard-link either API into the plugin jar.

### Movement gate

`MovementListener` handles `PlayerFailMoveEvent` only when the server rejection reason is `CLIPPED_INTO_BLOCK` and the player is confirmed as Bedrock.

The listener creates a `FailMoveContext` containing the requested movement and translated player bounding boxes. Enabled modules then evaluate the same context independently.

### Modules

Each movement parity fix implements `MovementModule`.

A module can:

- abstain when the movement is unrelated;
- mark a relevant candidate as blocked when a safety condition fails;
- allow the rejected movement when the mismatch is positively identified.

Modules expose counters for runtime testing.

### Powder snow

The initial module treats powder snow as a candidate only when it appears in the swept player volume. The module refuses to bypass the rejection when another collidable block overlaps that swept volume, an unloaded chunk is touched, the movement is implausibly large, or leather boots should preserve Java collision behavior.

## Next collision architecture

Bamboo and pointed dripstone require a stronger model than powder snow because the Bedrock client uses different position-dependent collision boxes.

The planned implementation has two halves:

1. Failed Java movement can be allowed only when the Java collision rejects the position but the calculated Bedrock collision does not.
2. Normal movement must be clipped against the calculated Bedrock collision so removing the Java-side false positive cannot create a new pass-through exploit.

The shared resolver for those modules will live outside platform-specific block adapters so additional parity modules can reuse it.
