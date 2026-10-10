# Roadmap

## 0.4.x

Current architecture:

- Translated block parity is adapter-based.
- Bamboo and pointed dripstone are independent adapters.
- Swimming false-clip recovery is runtime-validated against the monument trace and solid-wall controls.
- Bedrock spectator movement has a separate stall-triggered assist path rather than reusing collision parity.
- Always-on movement evidence is captured in bounded in-memory per-player trace buffers and can be exported on demand.

Next:

- Runtime-tune spectator assist thresholds against Bedrock input in open air, inside blocks and during vertical movement.
- Use recorder dumps from real players to discover the next reproducible parity mismatch instead of adding speculative block handlers.
- Perform a dedicated Folia runtime validation pass.
- Keep Leaf 26.3 as the primary runtime validation target for this line.

## Later

- Track Minecraft 26.4 as a separate compatibility update rather than silently compiling against a newer API.
- Prepare public release metadata and licensing after the project owner selects a license.
