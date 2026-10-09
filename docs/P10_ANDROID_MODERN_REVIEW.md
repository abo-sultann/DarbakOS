# P10 — GDN Android Modernization Review Gate

**Review updated:** 2026-10-10  
**Original review baseline:** `61129bb435a07daef6923501fd581994ce089eff`  
**Working branch:** `p10-review-fixes-20261006`  
**Latest fully verified implementation/UI baseline:** `93891857d16945be7200e80fa5d24afd5e10c2cf`  
**Verified CI:** GitHub Actions run `37982275776` / #367 — **PASS**

## Target contract
- **غضن | GDN** is the current product identity. Historical `DarbakOS` identifiers may remain only where migration/compatibility requires them.
- Android 17 / API37 is the production software target for P10.
- `compileSdk 37`, `targetSdk 37`, `minSdk 25`.
- Android 7.1 / API25 is a **Legacy Regression Floor only** and must not constrain modern UI/architecture.
- The exact production head unit remains unknown. Emulator success proves software behavior only and never approves physical USB, ACC, CANBUS, thermals, audio, GNSS or OEM startup.
- No generic BOOT/ACC assumption is allowed.

## Review matrix

| # | Review item | Software status | Verified result | Physical remainder |
|---|---|---|---|---|
| 1 | Location permissions | **PASS** | Coarse+Fine request contract; Precise/Approximate/Denied/revoked policy; clean Android 17 permission flow | UX/GNSS verification on actual unit |
| 2 | Audio/shared media | **PASS** | `READ_MEDIA_AUDIO`, MediaStore, Content URI playback, same-position Play/Pause, stale URI invalidation | Real USB unplug/reinsert/vendor storage — **PENDING — PHYSICAL HEAD UNIT** |
| 3 | App/OsmAnd visibility | **PASS** | Explicit package visibility; controlled installed/absent fixture integration | Real production OsmAnd build/data |
| 4 | GPS/trip startup | **PASS** | `StartupCoordinator` + `RuntimeStartPolicy`; portable USER_LAUNCH; no generic BootReceiver | ACC/boot/CANBUS startup adapter |
| 5 | GPS freshness | **PASS** | stale fix expiry prevents old speed remaining live | Tune against real GNSS cadence/tunnels |
| 6 | Trip storage failover | **PASS** | write failure → internal fallback + pending replay + visible state; max active uncommitted chunk 60 fixes | removable-media/full-storage/power-drop tests |
| 7 | Transition-specific CI | **PASS** | API37 build/lint + modern Android tests + API25 regression floor execute explicitly | hardware acceptance remains separate |
| 8 | Modern UI / Visual Quality foundation | **PASS** | GDN palette, responsive large-screen dimensions, dedicated large-head-unit styles, ModernHeadUnitTest; CI #367 green | final density/brightness/touch/thermal/readability tuning |
| 9 | Documentation / identity alignment | **PASS for P10 closeout set** | README, Current Status, Next Task and this review aligned to GDN/current target; historical evidence retained rather than rewritten | commissioning documentation after hardware arrives |

## Trip-data durability
`TripAutoRecorder` commits a maximum active chunk of 60 position fixes. Completed chunks are flushed/synchronized before `.part` promotion to `.dtrip`. The active in-memory chunk is therefore the sudden-loss exposure window: **up to 60 fixes**, approximately **60 seconds at nominal 1 Hz**, varying with actual fix cadence.

A normal storage I/O failure is different: pending points remain in memory, storage switches internally when possible, and those points are replayed. Real unplug/full-storage/abrupt power behavior remains a physical test.

## Modern UI disposition
The P10 software foundation no longer targets an enlarged Android 7 shell. The verified branch now carries:
- GDN identity palette and visible naming;
- responsive Full-HD-class large-screen dimensions;
- large-head-unit typography/action/card presentation overrides;
- Arabic/RTL-preserving functional layout contracts;
- CI protection for modern navigation/fit while retaining API25 regression coverage.

This is **software acceptance**, not final physical-screen acceptance. Exact production density, aspect ratio, brightness, touch ergonomics, GPU/animation performance and thermal behavior are **PENDING — PHYSICAL HEAD UNIT**.

## Compatibility-sensitive legacy identifiers
Do not mechanically rename these during P10 closeout:
- package namespace `com.abosultan.darbakos`;
- persisted preference/storage keys such as legacy local-media preferences;
- update-package compatibility names/contracts;
- class/component names referenced by Manifest/tests unless migrated atomically with coverage.

They are technical identifiers, not product branding. A later migration must include backward-compatibility tests.

## P10 software exit rule
P10 software review is ready for final PR closeout when:
1. current documentation head receives green CI;
2. PR #1 has no unresolved blocking review thread/comment;
3. final diff review finds no untested destructive/OEM assumption;
4. hardware-only items remain explicitly **PENDING — PHYSICAL HEAD UNIT**.

Only then should PR #1 leave Draft and be considered for merge to `main`.

## Physical commissioning order
Exact-device read-only baseline → bounded GDN APK smoke → TPMS/ESP32 → GNSS/TripRuntime → Media/OsmAnd → CANBUS/OEM → verified Recovery/Golden Backup → only then deeper startup/OEM integration.

No firmware/MCU/kernel flashing, destructive root, OEM hiding, boot replacement or system-app removal is approved before recovery readiness is proven.
