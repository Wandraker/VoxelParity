# Architecture

## Goal

VoxelParity corrects specific Bedrock-to-Java movement and collision mismatches without globally changing Java server physics.

## Invariants

- Java players are never modified by a Bedrock parity module.
- A module must be narrowly scoped to a reproducible edition mismatch.
- VoxelParity does not globally replace vanilla collision shapes.
- VoxelParity does not load chunks while validating movement.
- A rejected movement packet is never allowed when an unrelated solid or entity collision is present.
- A Java collision false positive is bypassed only when the translated Bedrock collision proves the target is valid.
- Removing a Java false positive must not permit an accepted target position to end inside the translated Bedrock collision.

## Bedrock detection

`BedrockPlayerDetector` discovers Floodgate first and Geyser-Spigot second. The integration is reflective so VoxelParity does not package either API.

## Failed movement

`MovementListener` handles `PlayerFailMoveEvent.CLIPPED_INTO_BLOCK` for confirmed Bedrock players.

Powder snow remains a dedicated failed-movement module because its mismatch is state-oriented rather than a simple translated box.

Bamboo and pointed dripstone are handled by `CollisionParityService`. The target player box is evaluated against both the Java collision shape and the calculated Bedrock collision shape.

A failed move is allowed only when:

- a supported Java collision shape overlaps the target;
- the corresponding Bedrock collision box does not overlap the target;
- no unrelated collidable block overlaps the target;
- no relevant entity collision overlaps the target;
- no unloaded chunk is involved.

## Accepted movement

A per-player parity layer must also enforce Bedrock collision on movement that Java would otherwise accept.

For Bedrock players, normal `PlayerMoveEvent` movement gathers translated collision boxes from the local swept bounds, but correction is only activated when the reported target player box actually overlaps one of those Bedrock boxes. This distinction matters because two consecutive legal Bedrock packet positions do not imply that the client physically traversed the straight line between them; treating that line as authoritative produced visible corner rubberbanding around bamboo and pointed dripstone.

When the target itself overlaps translated collision, `MotionResolver` clips movement axis-by-axis and returns the closest legal motion. Existing overlap is allowed to escape rather than trapping the player.

Teleport events are excluded.

## Geometry core

The geometry and movement solver are independent from Bukkit:

- `collision.math.Aabb`
- `collision.math.Motion`
- `collision.math.MotionResolver`
- `collision.math.AcceptedMoveGuard`
- `collision.bedrock.BedrockOffsetPattern`
- `collision.bedrock.BedrockBambooShape`
- `collision.bedrock.BedrockDripstoneShape`

The Paper layer adapts Bukkit block data into those core types.

## Bamboo scope

`Material.BAMBOO` is translated state-aware. Java `age=0` maps to the Bedrock thin stalk width (`2/16`) and Java `age=1` maps to the Bedrock thick stalk width (`3/16`). Both use the same coordinate-derived Bedrock offset algorithm. `Material.BAMBOO_SAPLING` is intentionally excluded until its Bedrock geometry is independently measured and tested.

## Pointed dripstone

Paper 26.3 exposes pointed dripstone through the generalized `Speleothem` block-data interface. VoxelParity maps `TIP`, `TIP_MERGE`, `FRUSTUM`, `MIDDLE` and `BASE` to Bedrock-specific widths and respects the vertical direction for tip height.


## Runtime diagnostics

Per-player debug mode includes movement-state probes that do not alter collision decisions. Powder-snow presence is logged only when the tracked state changes, while pose-change and failed-movement events capture the server-side pose, bounding box and related movement state. These probes exist to distinguish a real collision mismatch from a server/client pose transition mismatch.
