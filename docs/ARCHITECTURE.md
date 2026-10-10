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

Bamboo and pointed dripstone are handled by `CollisionParityService` through independent `TranslatedCollisionAdapter` implementations. `TranslatedBlockCollision` is a material-indexed adapter registry rather than a block-specific decision tree. Each translated block carries an explicit module kind so geometry, diagnostics and runtime metrics can be attributed independently. The target player box is evaluated against both the Java collision shape and the calculated Bedrock collision shape.

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

A real server-observed transition involving `Pose.SWIMMING` opens a short, location-bounded pose-transition window. While that window is active, a standing or sneaking target that overlaps translated collision is additionally evaluated as a 0.6-block-high compact target anchored at the same feet position. The compact path is accepted only when it is a strict subset of the normal target and is clear of translated collision. Failed-movement handling also requires the compact target to be clear of unrelated blocks, entities and unloaded chunks. This avoids fighting Geyser/Leaf crawl transitions without globally shrinking the player hitbox or forcing a pose.

Teleport events are excluded.

## Geometry core

The geometry and movement solver are independent from Bukkit:

- `collision.math.Aabb`
- `collision.math.Motion`
- `collision.math.MotionResolver`
- `collision.math.AcceptedMoveGuard`
- `collision.math.PoseAwareCollisionGuard`
- `collision.bedrock.BedrockOffsetPattern`
- `collision.bedrock.BedrockBambooShape`
- `collision.bedrock.BedrockDripstoneShape`

The Paper layer adapts Bukkit block data into those core types. Block-specific translation is isolated behind:

- `TranslatedCollisionAdapter`
- `BambooCollisionAdapter`
- `PointedDripstoneCollisionAdapter`
- `TranslatedBlockCollision`

Adding a future translated block should primarily require a new adapter and collision kind rather than changes to the shared movement resolver.

## Bamboo scope

`Material.BAMBOO` is translated state-aware. Java `age=0` maps to the Bedrock thin stalk width (`2/16`) and Java `age=1` maps to the Bedrock thick stalk width (`3/16`). Both use the same coordinate-derived Bedrock offset algorithm. `Material.BAMBOO_SAPLING` is intentionally excluded until its Bedrock geometry is independently measured and tested.

## Pointed dripstone

Paper 26.3 exposes pointed dripstone through the generalized `Speleothem` block-data interface. VoxelParity maps `TIP`, `TIP_MERGE`, `FRUSTUM`, `MIDDLE` and `BASE` to Bedrock-specific widths and respects the vertical direction for tip height.


## Runtime diagnostics

Per-player debug mode includes movement-state probes. Powder-snow presence is logged only when the tracked state changes, while pose-change and failed-movement events capture the server-side pose, bounding box and related movement state. Pose-compatible collision decisions are emitted separately as `[debug-pose-collision]` so they can be distinguished from ordinary geometry corrections. High-frequency accepted-movement pose logs are rate-limited, while failed-movement and actual pose-transition evidence remains unthrottled.


## Spectator assist

Spectator behavior is intentionally separated from collision parity. A Bedrock player in `SPECTATOR` never enters the normal failed-movement or translated-collision correction paths.

`SpectatorAssistService` consumes `PlayerInputEvent` state and tracks the player's actual progress on the entity scheduler. Normal client movement remains authoritative while progress is above the configured threshold. After a short stall, a bounded assist burst advances the player in the normalized requested direction. Forward/back movement respects camera pitch; strafe is yaw-relative; jump and sneak provide explicit vertical input.

Assist targets are rejected when the target chunk is not already loaded. Assisted movement uses asynchronous teleport completion and clears velocity while assisting so a client-side acceleration spike is not carried out of an obstruction.

## Movement trace recorder

`MovementTraceRecorder` is an always-on diagnostic ring buffer for confirmed Bedrock players. It stores selected movement evidence such as failed moves, non-trivial parity decisions, pose transitions, spectator input changes and spectator-assist activation. Normal accepted movement packets are not recorded.

Storage is bounded by `recorder.entries-per-player` and `recorder.max-players`. There is no continuous dump file and no continuous disk I/O. `/vp dump <player>` snapshots immutable trace entries and schedules the file write on Paper's async scheduler. The recorder buffers do not retain Player or world objects.
