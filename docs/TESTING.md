# VoxelParity 0.1.0-SNAPSHOT test plan

Target environment:

- Minecraft 26.3
- Java 25
- Leaf preferred
- Geyser + Floodgate where available

## Baseline

1. Start the server with VoxelParity installed.
2. Run `/vp status` and confirm the Bedrock detector is Floodgate or Geyser.
3. Run `/vp modules` and confirm `powder-snow: enabled=true`.

## Java control

1. Join with a Java client.
2. Enter and leave powder snow without leather boots.
3. Repeat with leather boots.
4. Confirm VoxelParity does not log or alter Java movement.

## Bedrock powder snow

1. Join through Geyser with a Bedrock client.
2. Run `/vp debug <player> on` from console or an operator account.
3. Enter deep powder snow without leather boots.
4. Attempt to move horizontally, sink, jump and escape.
5. Reproduce the position-rubberband case if possible.
6. Confirm that any corrected rejection logs `outcome=ALLOW` and `reason=powder-snow-only-collision-mismatch`.
7. Confirm the player is not repeatedly teleported to the same rejected position.

## Leather boots

1. Equip leather boots on the Bedrock player.
2. Repeat the powder-snow test.
3. Confirm VoxelParity does not bypass the movement rejection and debug shows `leather-boots-preserve-java-collision` when evaluated.

## Wall safety

1. Place powder snow immediately beside a full solid wall.
2. Attempt movement toward or through the wall from the Bedrock client.
3. Confirm VoxelParity does not allow a rejected move through the wall.
4. Debug should show `non-powder-snow-collision-in-swept-volume` for a relevant candidate.

## Unloaded chunk safety

1. Test near an unloaded chunk boundary without forcing the neighboring chunk to load.
2. Confirm VoxelParity does not load the chunk itself.
3. Confirm a candidate touching an unloaded chunk is blocked.

## Metrics

Run `/vp modules` after testing and record the `allowed` and `blocked` counters together with the relevant server log excerpt.
