# VoxelParity 0.4.1 test plan

Target environment:

- Minecraft 26.3
- Java 25
- Leaf preferred
- Geyser + Floodgate where available

## Build gate

1. GitHub Actions must pass on Java 25.
2. All geometry and motion unit tests must pass.
3. No license file should be added until a project license is selected.

## Baseline

1. Start the server with VoxelParity installed.
2. Run `/vp status` and confirm the Bedrock detector is Floodgate or Geyser.
3. Run `/vp modules` and confirm powder snow, bamboo and pointed dripstone are enabled.

## Java control

1. Join with a Java client.
2. Walk through bamboo, pointed dripstone and powder snow.
3. Confirm VoxelParity does not log, redirect or alter Java movement.
4. Repeat with nearby mobs and confirm their collision remains vanilla.

## Bedrock bamboo

1. Join through Geyser with a Bedrock client.
2. Enable `/vp debug <player> on`.
3. Test a tall `age=0` thin bamboo stalk from every side, including the previously reproducible catch case.
4. Test an `age=1` thick bamboo stalk from every side.
5. Slide around corners and then walk through dense and vertically stacked bamboo containing both ages.
6. Confirm `age=0` no longer gets clipped against a `3/16` translated box and false Java rubberband is removed where the Bedrock client has legal space.
7. Confirm diagonal movement around a free bamboo corner does not produce `outcome=CORRECT` when every `bedrockTargetOverlap` value is false.
8. Confirm the player is still corrected when the reported target position actually ends inside the thin `2/16` or thick `3/16` Bedrock stalk box.
9. Test `BAMBOO_SAPLING` separately and confirm it remains pass-through for Bedrock movement while VoxelParity leaves it untouched.
10. When mature bamboo still catches the player, keep the `[debug-geometry]` lines for that exact attempt. They include block coordinates, block data, Java collision boxes, the calculated Bedrock box and target overlap flags.

## Bedrock pointed dripstone

With debug enabled, keep `[debug-pose]` and `[debug-pose-collision]` lines if the player enters a crawl/swim-like pose under hanging dripstone. VoxelParity must not force a pose. The server/Geyser pose event remains authoritative.

Test each thickness where practical:

- TIP
- TIP_MERGE
- FRUSTUM
- MIDDLE
- BASE

For upward and hanging tips:

1. approach from all horizontal sides;
2. move diagonally around the edge;
3. jump into the tip region;
4. fall or descend alongside hanging dripstone;
5. confirm Java false-positive correction does not permit ending inside the Bedrock box;
6. repeatedly pass diagonally between nearby dripstone shapes and confirm no `MOVE_CORRECT` occurs when the target is free;
7. reproduce the hanging-tip crawl case and confirm a recent `SWIMMING` transition can produce `[debug-pose-collision] ... recent-swimming-target-clear` instead of repeated standing-box correction;
8. confirm the compact pose path does not allow movement through the side of a pointed-dripstone box or through unrelated solid/entity collision;
9. wait more than the short transition grace period and confirm ordinary standing collision enforcement resumes.

## Powder snow

1. Enable per-player debug before entering the test area.
2. Enter deep powder snow without leather boots and confirm a `[debug-snow]` transition records `ENTER` or `INSIDE`.
3. Attempt horizontal movement, sinking, jumping and escaping.
4. Reproduce the original position-rubberband case if possible.
5. If Leaf emits a failed movement, retain the `[debug-failmove]` line and confirm whether `targetPowderSnow=true`.
6. Confirm any corrected powder-snow rejection logs `outcome=ALLOW` from the powder-snow module.
7. Leave the snow and confirm a `[debug-snow]` `EXIT` transition.
8. Repeat with leather boots and confirm VoxelParity does not bypass the intended Java collision rule.

## Swimming false-clip recovery

1. Enable per-player debug.
2. Reproduce the previously observed underwater Bedrock movement where Leaf repeatedly emits `CLIPPED_INTO_BLOCK`.
3. Confirm genuinely clear swimming movement can produce `module=swimming-collision outcome=ALLOW reason=swimming-path-clear` instead of rubberbanding.
4. Re-test the previously observed corner where 0.4.0 produced repeated `java-collision-in-swept-volume`; a free axis-resolved path should no longer be rejected only because the broad swept union touches the corner.
5. Swim directly into a full solid wall and confirm the swimming module abstains with `java-target-collision` or `java-path-collision`.
6. Start sprint-swimming and watch for a brief `STANDING` pose while `isSwimming()` is already true; the compact swimming-intent path may recover a false clip without forcing pose.
7. Stop swimming in open water and confirm returning to `STANDING` remains normal.
8. Repeat beside powder snow and confirm the swimming module does not take ownership from the powder-snow module.
9. Keep `[debug-java-collision]` lines for any remaining rejected swimming movement.

## Spectator assist

1. Switch the Bedrock player to spectator mode.
2. Move normally in open air in every direction and confirm the assist does not constantly take over while client progress is healthy.
3. Reproduce the previous Bedrock spectator stall against or inside a solid structure.
4. Hold forward, backward, left, right, jump and sneak separately and confirm a stalled direction can trigger a short assist burst.
5. After an assist burst exits a block, keep holding the same input and confirm VoxelParity does not immediately start another burst every few ticks.
6. Test diagonal combinations and forward movement while looking sharply up and down.
7. Confirm leaving an obstruction does not produce the previous acceleration spike or a plugin-driven zigzag.
8. Confirm assist never targets an unloaded chunk.
9. Run `/vp modules` and confirm `spectator-assist` burst/step counters increase only when assistance actually activates.
10. Confirm spectator movement still never enters powder-snow, swimming or translated-collision parity decisions.

## Safety cases

### Solid wall

Place a translated block or powder snow directly beside a full solid wall. Attempt to use the parity correction to cross the wall. The move must remain blocked.

### Entity collision

Repeat a failed movement case while a collidable entity occupies the destination. The move must not be bypassed by translated collision parity.

### Unloaded chunk

Test near an unloaded chunk boundary. VoxelParity must not load the chunk itself and must not bypass a failed movement involving that unloaded area.

### Large movement

Use legitimate server mechanics or test tooling to create movement larger than the configured per-axis limit. VoxelParity must not use translated collision correction for that movement.

## Evidence to keep

For each failure or regression, keep:

- VoxelParity version and commit SHA;
- Leaf/Paper build;
- Geyser and Floodgate versions;
- Bedrock client version;
- `/vp modules` counters;
- relevant `[debug]` and `[debug-geometry]` lines;
- exact block coordinates and states;
- a short video when geometry is visually ambiguous.


## Movement trace recorder

1. Leave `recorder.enabled: true` with the default bounded limits.
2. Reproduce several swimming failures, pose changes and a spectator assist burst without enabling verbose debug.
3. Run `/vp dump <player>`.
4. Confirm the command returns immediately and the dump is written under `plugins/VoxelParity/dumps/`.
5. Confirm the file contains recent fail-move, decision, pose and spectator-assist evidence but does not contain continuous ordinary movement packets.
6. Disconnect the Bedrock player and confirm a recent trace can still be dumped by player name.
7. Generate more than the configured per-player entry limit and confirm the oldest entries are evicted rather than memory growing without bound.
