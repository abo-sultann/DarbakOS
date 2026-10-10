# 02_NEXT_TASK — Continue Build on Replaceable Hardware Defaults

P10 software modernization is **closed and merged to `main`**. P11 physical commissioning remains prepared, but development does **not** stop while waiting for the production head unit.

## Verified baseline
- P10 reviewed branch head: `855731e4ce23c5cdea117c552ac449703921a8f0`.
- Merge commit on `main`: `1e2cc0185653ea9dcacabb51d9128857f143befa`.
- Pre-merge CI run `37993383810` / #373: **SUCCESS**.
- Post-merge `main` CI run `38000534974` / #374: **SUCCESS**.
- Android 17 / API37 Production Target: PASS.
- API25 Legacy Regression Floor: PASS.
- Product identity: **غضن | GDN**.

## Development policy before the new screen arrives
Continue building GDN using the replaceable default profile in:

`app/src/main/java/com/abosultan/darbakos/core/HardwareProfile.java`

Current assumed development profile:
- Android production target: API37;
- legacy regression floor: API25;
- landscape development display: 1920×1080;
- assumed minimum RAM class: 4 GB;
- preferred RAM class: 8 GB;
- removable USB expected;
- internal GNSS expected;
- standard Android audio path expected.

These are **development assumptions, not measured hardware facts**. They are intentionally centralized so the production profile can replace them after P11 measurements without rebuilding unrelated GDN logic.

## Unknowns that must stay behind adapters
Do not hard-code guessed behavior for:
- ACC/sleep/wake;
- CANBUS/OEM interfaces;
- boot/autostart semantics;
- MCU/vendor services;
- recovery/partition behavior;
- exact audio routing;
- exact USB topology;
- exact display density/brightness/thermal characteristics.

The default profile must never authorize a generic BOOT_COMPLETED receiver or destructive platform changes.

## Continue now
Software work may continue on:
1. core architecture and state;
2. modern UI and responsive layouts;
3. Media/MediaStore/audio-focus logic;
4. GNSS/Trip logic and simulated/fake-source tests;
5. TPMS/ESP32 integration contracts;
6. Vehicle data foundation;
7. OsmAnd integration;
8. Apps/Settings/Admin surfaces;
9. diagnostics, observability and commissioning tools;
10. automated tests and failure simulation.

## Physical commissioning
The physical checklist remains in:

`docs/P11_PHYSICAL_COMMISSIONING_READINESS.md`

When the unit arrives, use the measured data to replace/default-tune the profile and implement only the hardware-specific adapters justified by evidence.

Physical acceptance remains **PENDING — PHYSICAL HEAD UNIT**.

## Safety boundary
Until Recovery + Golden Backup readiness is proven:
- no firmware/MCU/kernel flashing;
- no destructive root/system changes;
- no boot replacement or OEM app removal/hiding;
- no generic BOOT_COMPLETED/ACC startup logic;
- no namespace/applicationId migration;
- no persisted-key renaming without a migration plan and regression coverage.
