# Roadmap

## 0.3.x

Current architecture:

- Translated block parity is adapter-based.
- Bamboo and pointed dripstone are independent adapters.
- Shared movement resolution, pose handling, diagnostics and metrics do not contain block-specific geometry branches.
- Real Geyser runtime traces are preserved as regression tests.

Next:

- Add new parity adapters only for reproducible Bedrock/Java mismatches.
- Extend pose-aware handling only when additional real pose mismatches are reproduced.
- Perform a dedicated Folia runtime validation pass.
- Keep Leaf 26.3 as the primary runtime validation target for this line.

## Later

- Track Minecraft 26.4 as a separate compatibility update rather than silently compiling against a newer API.
- Prepare public release metadata and licensing after the project owner selects a license.
