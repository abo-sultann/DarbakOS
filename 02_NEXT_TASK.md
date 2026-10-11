# 02_NEXT_TASK — Continue Software Without Inventing Physical Hardware

P10 software modernization is **closed and merged to `main`**. P11 physical commissioning remains prepared and blocked on the production head unit. P12 software hardware-readiness is active and may continue only where behavior can be proven without guessing OEM hardware.

## Verified baseline
- Product identity: **غضن | GDN**.
- Production software target: **Android 17 / API37**.
- API25: **Legacy Regression Floor only**.
- Latest verified P12 gate: commit `a4f2a4edc4556de858fc05ddab5bf1878ddbf5bc`.
- CI run `38161177038` / #416: **SUCCESS**.
- Physical acceptance: **PENDING — PHYSICAL HEAD UNIT**.

## What is already established
The replaceable hardware-default architecture is now integrated through:
- `HardwareProfile` and `HardwareProfileStore`;
- `DeviceCapabilityPolicy`;
- profile-aware startup gating;
- profile-aware MediaStore scanning;
- `HardwareProfileDiagnostics`;
- `CommissioningState` for authoritative Admin readiness display.

Portable measurements and full physical commissioning are intentionally separate. A `MEASURED` portable profile must never imply that ACC, CANBUS, Boot/Wake or Recovery have been commissioned.

## Current development defaults
Current development assumptions remain centralized and replaceable:
- Android production target: API37;
- legacy regression floor: API25;
- landscape development display: 1920×1080;
- assumed minimum RAM class: 4 GB;
- preferred RAM class: 8 GB;
- removable media capability expected for development;
- internal GNSS expected for development;
- standard Android audio path expected for development.

These values are **ASSUMED**, not measured production facts. User-visible UI must not present them as confirmed hardware.

## Next software work
Continue only work that is independent of exact OEM behavior, in this order:
1. strengthen TPMS/ESP32 integration contracts and failure states without inventing the final USB/radio transport;
2. strengthen `VehicleSourceAdapter` boundaries and unavailable/stale-source behavior without guessed CAN IDs or protocols;
3. improve diagnostics and commissioning observability using read-only state;
4. add automated regression/failure-simulation tests for portable services;
5. keep responsive UI and user-visible wording independent of unverified USB/SD/ACC/CANBUS hardware;
6. keep exact hardware-specific adapters deferred until P11 measurements exist.

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

The assumed profile must never authorize generic `BOOT_COMPLETED`, guessed ACC broadcasts or destructive platform changes.

## Physical commissioning
The physical checklist remains in:

`docs/P11_PHYSICAL_COMMISSIONING_READINESS.md`

When the production unit arrives, collect measured evidence first, then replace/tune the profile and implement only adapters justified by that evidence.

Physical acceptance remains **PENDING — PHYSICAL HEAD UNIT**.

## Safety boundary
Until Recovery + Golden Backup readiness is proven:
- no firmware/MCU/kernel flashing;
- no destructive root/system changes;
- no boot replacement or OEM app removal/hiding;
- no generic BOOT_COMPLETED/ACC startup logic;
- no namespace/applicationId migration;
- no persisted-key renaming without a migration plan and regression coverage.
