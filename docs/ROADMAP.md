# Roadmap

## 0.2.x hardening

Completed:

- Runtime validation for thin/thick bamboo on Leaf 26.3.
- Runtime validation for pointed dripstone, corner movement and hanging-tip pose transitions.
- Live validation that `BAMBOO_SAPLING` remains pass-through.
- Recorded regression fixtures from real Geyser sessions.
- Per-block translated-collision metrics.
- Reduced high-frequency pose-compatible debug noise.

## 0.3.x

- Turn translated collision handlers into a cleaner extension point for future parity modules.
- Extend pose-aware handling only when additional real Bedrock/Java pose mismatches are reproduced.
- Evaluate additional collision mismatches only when a reproducible test case exists.
- Review Folia region-thread behavior separately from Paper/Leaf.

## Later

- Track Minecraft 26.4 as a separate compatibility update rather than silently compiling against a newer API.
- Prepare public release metadata and licensing after the project owner selects a license.
