# غضن | GDN — Current Status

**Updated:** 2026-10-10
**Authority:** GitHub repository state and verified CI evidence.

## Phase state
- **P0–P8:** CLOSED within documented software scope.
- **P9:** SOFTWARE READINESS PASS / PHYSICAL COMMISSIONING PENDING.
- **P10:** ACTIVE — final documentation/review stage.
  - Android-modern compatibility: **PASS — Android 17 / API37**.
  - Modern UI / Visual Quality foundation: **PASS in software CI**.
  - GDN identity migration: **ACTIVE**, with compatibility-sensitive legacy identifiers intentionally retained where required.
  - OEM/ACC/boot/USB/GNSS/audio/CANBUS physical integration: **PENDING — PHYSICAL HEAD UNIT**.
- **P11:** Not started; follows P10 closure and physical commissioning.

## Product identity
The current product identity is **غضن | GDN**. `DarbakOS` may remain in repository/package/class/preferences identifiers only where changing it creates migration or compatibility risk. It is not the current user-facing brand.

Bounded identity audit completed on 2026-10-10: safe visible/documentation remnants updated; compatibility identifiers and historical evidence retained. Scope, A/B/C classification and local validation: [`docs/GDN_IDENTITY_AUDIT.md`](docs/GDN_IDENTITY_AUDIT.md). This does not close P10 or approve a merge.

## Authoritative target
- Production software target: **Android 17 / API37**.
- Build: `compileSdk 37`, `targetSdk 37`, `minSdk 25`.
- Android 7.1 / API25: **Legacy Regression Floor only**.
- Retired Allwinner T3 / 1024×600 / ~1 GB hardware must not constrain the modern product.
- Exact production SoC/RAM/display/OEM/CANBUS/ACC profile remains intentionally unknown until the replacement head unit is physically inspected.

## P10 verified software gate
Working branch: `p10-review-fixes-20261006`  
Draft PR: #1 — `P10: Android 17 migration + review gate`  
Latest fully verified UI baseline commit: `93891857d16945be7200e80fa5d24afd5e10c2cf`  
Verified CI: run `37982275776` / #367 — **SUCCESS**.

Verified coverage includes:
- API37 Build + Lint and Android 17 emulator;
- API25 legacy regression floor;
- clean-install location permission flow and Precise/Approximate/Denied policy;
- `READ_MEDIA_AUDIO`, MediaStore and Content URI playback contracts;
- stale MediaStore URI invalidation;
- OsmAnd visibility/present/missing integration fixture;
- RuntimeStartPolicy + StartupCoordinator;
- GPS foreground runtime and stale-position expiry;
- trip-storage write failure/internal fallback/status reporting;
- modern head-unit layout/navigation gate;
- existing portable P4/P5 regression coverage.

## P10 disposition
1. **Location — PASS.** Precise/Approximate/Denied distinguished; trip runtime requires eligible precise location.
2. **Media/USB software — PASS.** Modern permission/MediaStore/URI behavior covered. Real removable-media unplug/reinsert: **PENDING — PHYSICAL HEAD UNIT**.
3. **Apps/OsmAnd — PASS in software.** Real production OsmAnd build remains a commissioning check.
4. **GPS Runtime — PASS in software.** No generic BootReceiver/ACC assumption.
5. **GPS Freshness — PASS.** Stale fixes expire instead of remaining falsely live.
6. **Trip Storage — PASS in software.** External failure falls back internally with retained pending points and visible state. Maximum active uncommitted chunk = 60 fixes; approximately 60 seconds at nominal 1 Hz.
7. **Transition CI — PASS.** Focused modernization tests execute explicitly.
8. **Modern UI / Visual Quality — PASS as software foundation.** GDN palette, responsive large-screen dimensions and dedicated large-head-unit presentation styles are CI-green. Final density/brightness/touch/thermal/readability tuning: **PENDING — PHYSICAL HEAD UNIT**.
9. **Documentation / GDN migration — ACTIVE.** README and current status aligned; remaining project records are being normalized before final P10 review.

## Physical commissioning
Emulator success is not physical approval. The following remain **PENDING — PHYSICAL HEAD UNIT**:
- exact display fit/density/brightness/thermal behavior;
- real USB mount/unmount/remount;
- physical GNSS;
- audio routing/amplifier behavior;
- ACC/boot/wake/autostart semantics;
- CANBUS/OEM integration;
- recovery readiness and Golden Backup/hash.

Required order before destructive platform work: exact-device baseline → bounded GDN APK smoke → TPMS/ESP32 → GPS → Media/OsmAnd → CANBUS/OEM → verified recovery/Golden Backup → only then deeper startup/OEM integration.

GitHub remains the project-state authority. Historical evidence remains in `TEST_RESULTS.md` and `docs/test-evidence/`.
