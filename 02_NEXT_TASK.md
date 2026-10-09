# 02_NEXT_TASK — P11 Physical Commissioning Readiness

P10 software modernization is **closed and merged to `main`**. Do **not** reopen broad Android-modern compatibility, identity cleanup or UI-foundation work unless a concrete regression appears.

## Verified baseline
- P10 reviewed branch head: `855731e4ce23c5cdea117c552ac449703921a8f0`.
- Merge commit on `main`: `1e2cc0185653ea9dcacabb51d9128857f143befa`.
- Pre-merge CI run `37993383810` / #373: **SUCCESS**.
- Post-merge `main` CI run `38000534974` / #374: **SUCCESS**.
- Android 17 / API37 Production Target: PASS.
- API25 Legacy Regression Floor: PASS.
- Product identity: **غضن | GDN**.

## Current next gate
The next approved gate is now documented in:

`docs/P11_PHYSICAL_COMMISSIONING_READINESS.md`

P11 is **ready to start only when the exact production head unit is physically available**. Until then, software changes must remain bounded to proven defects or explicitly approved standalone features; no hardware behavior may be invented.

## Commissioning order
1. read-only device baseline;
2. bounded GDN APK smoke;
3. display/touch acceptance;
4. TPMS/ESP32 connectivity;
5. GNSS/TripRuntime;
6. Media/USB/OsmAnd;
7. CANBUS/OEM discovery;
8. ACC/boot/wake characterization;
9. Recovery readiness;
10. Golden Backup/hash where possible.

All steps are currently **PENDING — PHYSICAL HEAD UNIT**.

## Explicitly not approved yet
- firmware/MCU/kernel flashing;
- destructive root/system changes;
- boot replacement or OEM app removal/hiding;
- generic BOOT_COMPLETED/ACC startup logic;
- namespace/applicationId migration;
- persisted-key renaming without a migration plan and regression coverage.

Only after Recovery + Golden Backup readiness is proven may deeper OEM/startup/platform modification be proposed as a separate reviewed scope.
