# P10 — Android Modernization Review Gate

**Review date:** 2026-10-07  
**Review baseline:** `61129bb435a07daef6923501fd581994ce089eff`  
**Working branch:** `p10-review-fixes-20261006`  
**Latest verified implementation:** `77da6df90bbfffc869a927ea39673ef9220c4cf2`  
**Verified CI:** GitHub Actions run `37667936775` — **PASS**  
**Evidence artifact:** `DarbakOS-compatibility-7eec5071135cf3a39c0d90193b54ce71c6ce8c3b` / artifact `11504071154`

## Target contract

- Android 17 / API 37 is the primary software target for P10.
- `compileSdk 37`, `targetSdk 37`, `minSdk 25`.
- Android 7 / API 25 is a regression floor only and must not constrain the modern UI or architecture.
- The exact production head unit is still unknown. Emulator success proves software compatibility only; it does **not** approve a physical head unit, USB implementation, ACC behavior, CANBUS integration, thermals, audio hardware, GPS hardware, or OEM startup behavior.
- No generic BOOT/ACC assumption is allowed. OEM/ACC integration remains behind an explicit hardware-specific boundary.
- Modern UI / Visual Quality remains an independent P10 acceptance gate after the compatibility review below.

## Review matrix

| # | Review item | Status | Implementation / change reference | Verified result | Physical-device remainder |
|---|---|---|---|---|---|
| 1 | Location permissions | **Fixed and tested** | `LocationPermissionPolicy`, `MainActivity`, `StartupCoordinator`; clean-dialog smoke `scripts/p10_permission_dialog_smoke.py`; permission test added in `51d2eab0`; final dialog hardening in `26acf0a5` | CI `37667936775` PASS. Real Android 17 permission UI covers Approximate, Precise, denial/withdrawal; runtime starts only with precise permission while Activity is eligible | Recheck UX and GPS hardware behavior on production head unit |
| 2 | Audio permission / shared audio / resume | **Fixed in software; physical USB pending** | Legacy `READ_EXTERNAL_STORAGE` limited to API<=32; `READ_MEDIA_AUDIO` for modern Android; `MediaPermissionPolicy`; `MediaStoreAudioScanner`; URI-backed `LocalMediaTrack`; `LocalMediaPlayer.playPause()` preserves current player position; `scripts/p10_media_permission_smoke.py` final isolation in `77da6df9` | CI `37667936775` PASS. Real Android 17 audio-permission dialog tested. MediaStore test covers insert → discover → remove → absent → reinsert → discover | Real USB mount/unmount/remount and vendor storage exposure must be tested on production head unit |
| 3 | App discovery and OsmAnd visibility | **Fixed and tested against controlled fixture** | Manifest `<queries>` declares launcher discovery and OsmAnd package variants; `OsmAndBridge`; `osmandfixture`; absence and visibility integration tests | CI `37667936775` PASS. Installed/absent visibility paths and fixture launch/navigation contract pass | Validate against the exact OsmAnd build installed on the production head unit and real returned navigation data |
| 4 | GPS/trip runtime startup | **Fixed for portable/user-launch path; OEM startup deferred** | `MainActivity` routes runtime startup through `StartupCoordinator`; `RuntimeStartPolicy`; only `USER_LAUNCH` is portable; no generic boot receiver; background/OsmAnd smoke included | CI `37667936775` PASS. RuntimeStartPolicy and StartupCoordinator tests are explicitly invoked by CI; runtime remains active while fixture OsmAnd is foregrounded and after returning | ACC/boot/CANBUS startup adapter requires exact head-unit behavior and recovery validation |
| 5 | Stale speed prevention | **Fixed and tested** | `PositionStore` enforces freshness via `PositionQualityPolicy.DEFAULT_FRESH_MS` (8 s) and publishes unavailable when data ages out, even if provider remains nominally enabled | CI `37667936775` PASS via `PositionFreshnessTest` | Confirm timeout feel against real GNSS update cadence/tunnels/weak-signal behavior |
| 6 | Trip storage failure/failover | **Fixed in software; physical media pending** | `TripRuntimeService` detects write failure, captures pending points, falls back to internal storage, replays uncommitted points, exposes `TripStorageState`; `TripAutoRecorder.MAX_POINTS_PER_CHUNK=60`; writer uses flush + fsync + atomic-style `.part`→`.dtrip` rename | CI `37667936775` PASS via `TripStorageFailoverTest`; UI surfaces fallback/write failure | Real removable-storage unplug/full-media tests on production head unit. Sudden-power-loss exposure is at most the current uncommitted 60-point chunk; at nominal 1 Hz ≈ up to 60 s, scaling with actual fix rate |
| 7 | Transition-specific CI | **Fixed and running** | Workflow explicitly runs real permission dialogs, media permission, OsmAnd absent/present, background service continuity, RuntimeStartPolicy, StartupCoordinator, stale position, storage failover, MediaStore discovery, modern display, plus API25 regression floor | CI `37667936775` PASS end-to-end; API37 build/lint PASS; API25 regression PASS | Hardware-only acceptance remains separate, not simulated |
| 8 | Documentation alignment | **In progress in this documentation batch** | This review gate plus README/status/next-task/test-result/changelog alignment | Documentation commits are not treated as physical acceptance | Exact head-unit commissioning docs will be completed only after hardware arrives |

## Power-loss and trip-data durability note

`TripAutoRecorder` commits at a maximum of 60 points per chunk. A completed chunk is flushed and synchronized before its temporary file is promoted to the final `.dtrip` file. Therefore already committed chunks are designed to survive process/device interruption. The active in-memory chunk remains the exposure window: **up to 60 position fixes**. At a nominal 1 Hz GPS cadence that is approximately **up to 60 seconds**, but the time duration changes with the real fix frequency.

A normal storage I/O failure is handled differently from sudden power loss: the runtime retains the uncommitted points in memory, switches to internal storage when possible, and replays those points. Physical unplug/full-storage and abrupt ACC/power-drop behavior still require production-head-unit testing.

## Modern UI / Visual Quality gate — not closed yet

The compatibility review above does not close P10. The current UI still carries visual/layout decisions inherited from the early low-resource target. The next software batch must modernize the visual system for a strong modern head unit without assuming an exact screen model:

- Full-HD-and-up responsive landscape composition.
- Arabic RTL as a first-class layout, not a mirrored afterthought.
- Strong hierarchy for speed, navigation, media, and vehicle state.
- Remove the heavy legacy top-bar feel; use a cleaner automotive information architecture.
- Transparent/lightweight lower navigation where appropriate.
- Day/night readability and restrained motion/layer effects that make use of modern GPU capability without sacrificing driving legibility.
- Keep API25 compatibility as regression-only; never downgrade the modern visual design to satisfy 1024×600/1 GB hardware.
- Final density, touch-target, brightness, thermal and animation tuning remain part of physical commissioning.

## P10 exit rule

P10 may be marked complete only when:

1. The Android-modern compatibility gate remains green.
2. Documentation matches the current code and test evidence.
3. Modern UI / Visual Quality gate is implemented and verified in modern emulator/screenshots.
4. Hardware-specific ACC/boot/USB/GNSS/audio/CANBUS items are explicitly marked **pending physical commissioning**, not falsely passed.

Physical head-unit acceptance itself belongs to commissioning and must never be inferred from emulator success.
