# غضن | GDN — Current Status

**Updated:** 2026-10-10  
**Authority:** GitHub repository state and verified CI evidence.

## Phase state
- **P0–P8:** CLOSED within documented software scope.
- **P9:** SOFTWARE READINESS PASS / PHYSICAL COMMISSIONING PENDING.
- **P10:** **SOFTWARE CLOSED / MERGED TO MAIN**.
  - Android-modern compatibility: **PASS — Android 17 / API37**.
  - Modern UI / Visual Quality software foundation: **PASS**.
  - GDN identity audit/migration: **PASS within bounded safe scope**; compatibility-sensitive legacy identifiers intentionally retained.
  - PR #1 merged to `main` at `1e2cc0185653ea9dcacabb51d9128857f143befa`.
  - Post-merge `main` CI run `38000534974` / #374: **SUCCESS**.
- **P11:** **READINESS PREPARED / EXECUTION BLOCKED ON HARDWARE**.
  - Commissioning gate: [`docs/P11_PHYSICAL_COMMISSIONING_READINESS.md`](docs/P11_PHYSICAL_COMMISSIONING_READINESS.md).
  - No physical item may be marked PASS before measurement on the exact production unit.
  - Current state for all hardware gates: **PENDING — PHYSICAL HEAD UNIT**.

## Product identity
The current product identity is **غضن | GDN**. `DarbakOS` may remain in repository/package/class/preferences/artifact identifiers only where changing it creates migration, persistence, Android-component or external-consumer risk. These retained identifiers are technical compatibility debt, not current branding.

Bounded identity audit: [`docs/GDN_IDENTITY_AUDIT.md`](docs/GDN_IDENTITY_AUDIT.md).

## Authoritative target
- Production software target: **Android 17 / API37**.
- Build: `compileSdk 37`, `targetSdk 37`, `minSdk 25`.
- Android 7.1 / API25: **Legacy Regression Floor only**.
- Retired Allwinner T3 / 1024×600 / ~1 GB hardware must not constrain the modern product.
- Exact production SoC/RAM/display/OEM/CANBUS/ACC profile remains intentionally unknown until the replacement head unit is physically inspected.

## P10 final verified gate
Final reviewed branch head before merge: `855731e4ce23c5cdea117c552ac449703921a8f0`  
Pre-merge CI: run `37993383810` / #373 — **SUCCESS**  
Merge commit on `main`: `1e2cc0185653ea9dcacabb51d9128857f143befa`  
Post-merge CI: run `38000534974` / #374 — **SUCCESS**.

Verified coverage includes:
- API37 Build + Lint and Android 17 emulator;
- API25 Legacy Regression Floor;
- clean-install location permission flow and Precise/Approximate/Denied/revoked policy;
- `READ_MEDIA_AUDIO`, MediaStore and Content URI playback contracts;
- stale MediaStore URI invalidation;
- OsmAnd visibility/present/missing integration fixture;
- RuntimeStartPolicy + StartupCoordinator with no generic BootReceiver/ACC assumption;
- GPS foreground runtime and stale-position expiry;
- trip-storage write failure/internal fallback/status reporting;
- modern head-unit layout/navigation gate;
- bounded GDN identity cleanup without namespace/persistence migration;
- existing portable regression coverage retained.

## P10 final disposition
1. **Location — PASS.**
2. **Media/USB software — PASS.** Real removable-media unplug/reinsert: **PENDING — PHYSICAL HEAD UNIT**.
3. **Apps/OsmAnd — PASS in software.** Real installed OsmAnd remains commissioning verification.
4. **GPS Runtime — PASS in software.**
5. **GPS Freshness — PASS.**
6. **Trip Storage — PASS in software.** Maximum active uncommitted chunk = 60 fixes; approximately 60 seconds at nominal 1 Hz.
7. **Transition CI — PASS.**
8. **Modern UI / Visual Quality software foundation — PASS.** Final density/brightness/touch/thermal/readability tuning: **PENDING — PHYSICAL HEAD UNIT**.
9. **Documentation / GDN migration — PASS for P10 software closure.** Historical evidence is intentionally preserved.

## P11 commissioning sequence
When the exact production unit is available:
1. read-only device baseline;
2. bounded GDN APK smoke;
3. display/touch acceptance;
4. TPMS/ESP32 connectivity;
5. GNSS/TripRuntime;
6. Media/USB/OsmAnd;
7. CANBUS/OEM discovery;
8. ACC/boot/wake characterization;
9. Recovery readiness;
10. Golden Backup/hash.

Before steps 9–10 are proven: no firmware/MCU/kernel flashing, destructive root, OEM app removal, boot replacement or guessed startup integration.

GitHub remains the project-state authority. Historical evidence remains in `TEST_RESULTS.md` and `docs/test-evidence/`.
