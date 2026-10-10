# Roadmap

## 0.2.x

- Validate state-aware thin/thick bamboo and pointed-dripstone parity on Leaf 26.3 with real Bedrock movement traces.
- Validate corner sliding, vertical movement and stacked bamboo columns.
- Measure `BAMBOO_SAPLING` separately before deciding whether it needs translation.
- Split shared collision counters into per-block-module metrics.

## 0.3.x

- Harden the resolver against additional player poses and unusual bounding boxes.
- Add recorded regression fixtures from real Geyser sessions.
- Evaluate additional collision mismatches only when a reproducible test case exists.
- Review Folia region-thread behavior separately from Paper/Leaf.

## Later

- Track Minecraft 26.4 as a separate compatibility update rather than silently compiling against a newer API.
- Prepare public release metadata and licensing after the project owner selects a license.
