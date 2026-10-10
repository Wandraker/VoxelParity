# Roadmap

## 0.4.x

Current architecture:

- Translated block parity is adapter-based.
- Bamboo and pointed dripstone are independent adapters.
- Swimming false-clip recovery uses detailed Java collision boxes and axis-resolved path validation rather than a broad swept-union collision veto.
- Brief swimming-intent/pose races can use compact geometry without forcing player pose.
- Bedrock spectator movement has a separate stall-triggered assist path with a post-assist observation window.
- Always-on movement evidence is captured in bounded in-memory per-player trace buffers and can be exported on demand.

Next:

- Runtime-validate 0.4.1 against the recorded spectator zigzag case and the recorded underwater corner-stall case.
- Use recorder dumps from real players to discover the next reproducible parity mismatch instead of adding speculative block handlers.
- Perform a dedicated Folia runtime validation pass.
- Keep Leaf 26.3 as the primary runtime validation target for this line.

## Later

- Track Minecraft 26.4 as a separate compatibility update rather than silently compiling against a newer API.
- Prepare public release metadata and licensing after the project owner selects a license.
