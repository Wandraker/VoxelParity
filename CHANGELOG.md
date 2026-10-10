# Changelog

## 0.2.6

- Split translated-collision metrics into independent bamboo and pointed-dripstone counters.
- Added explicit translated-collision kinds so diagnostics and metrics no longer depend on shared anonymous boxes.
- Reduced repeated pose-compatible accepted-movement debug spam while keeping failed-movement and pose-transition evidence intact.
- Added dedicated runtime regression fixtures based on real Bedrock/Geyser traces for the false corner-correction case and hanging-dripstone compact-pose case.
- Added unit coverage for per-module metric separation.
- Preserved the 0.2.5 movement, pose-transition, bamboo, pointed-dripstone and powder-snow behavior.
- Switched the project version to a normal release-style version without the `-SNAPSHOT` suffix.

## 0.2.5-SNAPSHOT

- Added pose-aware translated collision handling for Bedrock crawl/swimming transitions near hanging collision.
- Tracks only real server-observed transitions involving the `SWIMMING` pose and keeps a short, local grace window around the transition point.
- During that grace window, standing or sneaking targets that overlap translated collision are rechecked with a 0.6-block-high compact target box.
- Failed movement can now be allowed when the normal target overlaps translated Bedrock collision but the compact target is clear of translated collision, unrelated blocks, entities and unloaded chunks.
- Accepted movement no longer receives a translated-collision correction when the normal target overlaps but the recent compact pose target is clear.
- Added dedicated pose-compatible counters and `[debug-pose-collision]` traces.
- Added regression tests for the hanging pointed-dripstone runtime trace and lateral-collision safety.
- Does not force, spoof or cancel player poses; Geyser/Leaf remains authoritative for the actual pose transition.
- Preserved existing bamboo, pointed-dripstone geometry and powder-snow behavior.
- Updated the project version to `0.2.5-SNAPSHOT`.

## 0.2.4-SNAPSHOT

- Added non-invasive runtime diagnostics for powder snow and player pose transitions.
- Debug mode now logs powder-snow enter/exit state without changing movement behavior.
- Debug mode now records every Bedrock `PlayerFailMoveEvent` with fail reason, target powder-snow presence, pose and bounding-box state.
- Added Bedrock pose-change diagnostics with current/new pose, bounding-box dimensions, sneak/swim state and nearby powder snow or pointed dripstone.
- Added player pose and bounding-box state to translated-collision debug decisions and accepted-movement corrections.
- Confirmed through live Bedrock testing that `BAMBOO_SAPLING` remains pass-through and should stay outside the translated collision module.
- Updated the project version to `0.2.4-SNAPSHOT`.

## 0.2.3-SNAPSHOT

- Fixed false movement corrections while Bedrock players slide diagonally around bamboo and pointed dripstone.
- Accepted movement now triggers the translated-collision guard only when the reported target player box actually overlaps a Bedrock collision box.
- Stopped treating a straight swept segment between two legal packet positions as proof that the Bedrock client crossed a collision box.
- Added a regression fixture from the real pointed-dripstone runtime trace that previously produced a visible correction despite a legal target position.
- Preserved failed-movement Java-versus-Bedrock mismatch handling and existing state-aware bamboo geometry.
- Updated the project version to `0.2.3-SNAPSHOT`.

## 0.2.2-SNAPSHOT

- Fixed Bedrock collision geometry for thin Java bamboo states.
- Java bamboo `age=0` now maps to the Bedrock thin stalk width of `2/16`.
- Java bamboo `age=1` keeps the Bedrock thick stalk width of `3/16`.
- Kept the existing deterministic Bedrock bamboo position offset for both stalk thicknesses.
- Added unit coverage for thin/thick bamboo widths and Java age-to-thickness mapping.
- Kept `BAMBOO_SAPLING` outside the translated collision module.
- Updated the project version to `0.2.2-SNAPSHOT`.

## 0.2.1-SNAPSHOT

- Added detailed translated-collision geometry diagnostics for runtime Bedrock testing.
- Debug traces now report the exact translated block, coordinates, block data, Java collision boxes, calculated Bedrock collision box, player start/target/swept boxes and overlap results.
- Added geometry traces for both failed-movement decisions and accepted-movement corrections.
- Kept the collision geometry unchanged so mature bamboo failures can be measured before tuning offsets or dimensions.
- Updated the project version to `0.2.1-SNAPSHOT`.

## 0.2.0-SNAPSHOT

- Added a shared translated-collision subsystem for Bedrock-specific block geometry.
- Added deterministic Bedrock position offsets for translated collision boxes.
- Added per-player bamboo collision parity without changing Java player or mob collision.
- Added per-player pointed-dripstone collision parity using the Paper 26.3 `Speleothem` API.
- Added a normal-movement collision guard to prevent pass-through exploits after Java-side false-positive collision is bypassed.
- Hardened existing-overlap handling so a player can escape a translated box but cannot tunnel through it in the opposite direction.
- Added entity, unrelated-block and unloaded-chunk safety checks for failed-movement correction.
- Added collision geometry and motion resolver unit tests.
- Added a Java 25 GitHub Actions build with a downloadable plugin artifact.
- Kept bamboo saplings outside the translated collision module until their Bedrock geometry is separately verified.
- Updated the project version to `0.2.0-SNAPSHOT`.

## 0.1.0-SNAPSHOT

- Created the initial VoxelParity project for Java 25 and Paper API 26.3.
- Added reflective Floodgate/Geyser Bedrock player detection without a hard runtime API dependency.
- Added the first powder-snow movement parity module.
- Added swept-volume collision safety checks that do not load chunks.
- Added leather-boots protection for powder-snow behavior.
- Added per-player debug logging and module counters.
- Added `/vp status`, `/vp modules`, `/vp debug`, and `/vp reload`.
