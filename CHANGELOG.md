# Changelog

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
