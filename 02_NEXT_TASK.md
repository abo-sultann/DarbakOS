# 02_NEXT_TASK — GDN P10 Closeout

Android-modern compatibility and the modern GDN UI foundation are green. Do **not** repeat broad compatibility or UI-foundation work unless a concrete regression appears.

## Verified baseline
- Branch: `p10-review-fixes-20261006`.
- Latest fully verified UI baseline: `93891857d16945be7200e80fa5d24afd5e10c2cf`.
- CI run `37982275776` / #367: **SUCCESS**.
- API37 Build/Lint: PASS.
- API25 Legacy Regression Floor: PASS.
- P10 software review items 1–8: PASS within stated scope.
- Current product identity: **غضن | GDN**.

## Next software task — close P10
1. Finish documentation alignment: `TEST_RESULTS.md`, `CHANGELOG.md` where present, and `docs/P10_ANDROID_MODERN_REVIEW.md`.
2. **DONE — 2026-10-10:** bounded `Darbak` identity audit; safe visible/documentation remnants updated. See [`docs/GDN_IDENTITY_AUDIT.md`](docs/GDN_IDENTITY_AUDIT.md) for retained compatibility identifiers and untouched history. No later task is authorized by this audit.
3. Preserve compatibility-sensitive identifiers unless a tested migration exists, including package namespace, persisted preference names and any update-package compatibility contract.
4. Run final CI on the documentation/identity head and require green API25 + API37 gates.
5. Review PR #1 for unresolved review threads/comments and final diff risks.
6. Only after the P10 Quality Gate is fully documented and CI-green: decide whether PR #1 is ready to leave Draft and merge to `main`.

## Physical work remains deferred
When the exact production head unit is available:
1. Capture read-only Android/API/SoC/RAM/display/build/OEM/CANBUS baseline.
2. Run bounded GDN APK smoke.
3. Verify real USB mount/unmount/remount, TPMS/ESP32, GNSS/TripRuntime, audio routing and real OsmAnd.
4. Measure ACC/boot/wake/autostart before any OEM startup adapter.
5. Verify Recovery and Golden Backup/hash before destructive changes.
6. Only then evaluate deeper OEM/CANBUS/autostart integration.

All hardware-specific acceptance above is **PENDING — PHYSICAL HEAD UNIT**.

No generic BootReceiver, firmware/MCU/kernel flashing, destructive root, OEM hiding, boot replacement or system-app removal is approved before recovery readiness is proven.
