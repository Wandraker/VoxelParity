# Changelog

## 0.2.0-SNAPSHOT

- Added a shared translated-collision subsystem for Bedrock-specific block geometry.
- Added deterministic Bedrock position offsets for translated collision boxes.
- Added per-player bamboo collision parity without changing Java player or mob collision.
- Added per-player pointed-dripstone collision parity using the Paper 26.3 `Speleothem` API.
- Added a normal-movement collision guard to prevent pass-through exploits after Java-side false-positive collision is bypassed.
- Added entity, unrelated-block and unloaded-chunk safety checks for failed-movement correction.
- Added collision geometry and motion resolver unit tests.
- Added a Java 25 GitHub Actions build.
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
