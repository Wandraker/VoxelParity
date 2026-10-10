# VoxelParity 0.2.2-SNAPSHOT test plan

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
7. Confirm the player cannot cross either the thin `2/16` or thick `3/16` calculated Bedrock stalk box.
8. Test `BAMBOO_SAPLING` separately and confirm VoxelParity currently leaves it untouched.
9. When mature bamboo still catches the player, keep the `[debug-geometry]` lines for that exact attempt. They include block coordinates, block data, Java collision boxes, the calculated Bedrock box and target overlap flags.

## Bedrock pointed dripstone

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
5. confirm Java false-positive correction does not permit crossing the Bedrock box.

## Powder snow

1. Enter deep powder snow without leather boots.
2. Attempt horizontal movement, sinking, jumping and escaping.
3. Reproduce the original position-rubberband case if possible.
4. Confirm corrected rejections log `outcome=ALLOW` from the powder-snow module.
5. Repeat with leather boots and confirm VoxelParity does not bypass the intended Java collision rule.

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
