# Roadmap

## 0.2.x

- Validate state-aware thin/thick bamboo and pointed-dripstone parity on Leaf 26.3 with real Bedrock movement traces.
- Validate corner sliding, vertical movement and stacked bamboo columns.
- Keep `BAMBOO_SAPLING` pass-through and outside translated collision after live Bedrock validation.
- Split shared collision counters into per-block-module metrics.

## 0.3.x

- Extend pose-aware handling only when additional real Bedrock/Java pose mismatches are reproduced.
- Add recorded regression fixtures from real Geyser sessions.
- Evaluate additional collision mismatches only when a reproducible test case exists.
- Review Folia region-thread behavior separately from Paper/Leaf.

## Later

- Track Minecraft 26.4 as a separate compatibility update rather than silently compiling against a newer API.
- Prepare public release metadata and licensing after the project owner selects a license.
