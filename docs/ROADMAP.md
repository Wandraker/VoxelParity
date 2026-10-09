# Roadmap

## 0.1.x

- Validate the powder-snow rubberband fix on Leaf 26.3 with real Bedrock movement traces.
- Record false positives and false negatives through `/vp debug`.
- Tighten the powder-snow candidate rules from runtime evidence if required.

## 0.2.x

- Add measured Bedrock bamboo collision boxes.
- Add measured Bedrock pointed dripstone collision boxes.
- Add a shared Bedrock collision resolver.
- Add a normal-movement guard so translated collisions cannot be bypassed.
- Add geometry and movement regression tests.

## 0.3.x

- Separate generic movement parity infrastructure from block-specific modules.
- Add runtime diagnostics that report Java collision, Bedrock collision and the final movement decision.
- Evaluate additional Geyser movement mismatches only when a reproducible test case exists.

## Later

- Review Paper, Purpur, Leaf and Folia behavior independently where platform-specific APIs can improve correctness.
- Prepare public release metadata and licensing after the project owner selects a license.
