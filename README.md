# Darbak OS

Unified in-car operating experience for a modern Android head unit.

## Current target
- **Primary software target:** Android 17 / API 37.
- **Current generic modern test profile:** landscape Full HD class; exact production display/SoC/RAM/OEM profile remains intentionally unknown until the replacement head unit is selected and physically inspected.
- **Legacy regression floor:** Android 7.1 / API 25. The former Allwinner T3 / 1024×600 / ~1 GB platform is no longer the production target and must not constrain modern architecture, UI or features.
- Arabic RTL, offline-first operation and truthful unavailable states remain product requirements.

## Current phase
P0–P8 are closed within their documented software scopes. P9 modern software readiness is proven, while physical commissioning remains pending the exact production head unit. P10 is active: Android-modern compatibility is now green on API37; **Modern UI / Visual Quality** is the next software gate, while ACC/boot/USB/GNSS/audio/CANBUS/OEM acceptance remains hardware-specific.

See the current P10 review record: [`docs/P10_ANDROID_MODERN_REVIEW.md`](docs/P10_ANDROID_MODERN_REVIEW.md).

## Start here
1. `01_CURRENT_STATUS.md`
2. `02_NEXT_TASK.md`
3. `docs/P10_ANDROID_MODERN_REVIEW.md`
4. `00_Darbak_OS_Master_Plan_Work_v1.0.md`
5. `REFERENCES.md`
6. `TEST_RESULTS.md`

## Critical hardware rule
Emulator success proves software compatibility only. It does **not** approve a production head unit. No firmware/MCU/kernel flashing, destructive root, OEM hiding, boot replacement, system-app removal or other destructive platform change is allowed before the exact production unit has a read-only baseline, verified recovery path and Golden Backup/hash evidence.

Generic BOOT_COMPLETED/ACC behavior is not assumed. OEM startup, CANBUS, removable-media behavior, physical GPS/audio and final screen-density/thermal tuning are commissioned only against the actual device.

## Latest compatibility evidence
GitHub Actions run `37667936775` on branch `p10-review-fixes-20261006` passed the Android 17/API37 build/lint and modern review gate plus the API25 legacy regression floor. Evidence artifact: `11504071154`.

GitHub is the durable project-state authority.
