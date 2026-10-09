# Changelog

## 2026-10-10 — GDN P10 modernization software closeout
- Adopted **غضن | GDN** as the current product identity while retaining compatibility-sensitive historical identifiers where required.
- Production software target is Android 17 / API37; API25 remains a Legacy Regression Floor only.
- P10 software gates cover location transitions, modern shared-media/MediaStore/Content URI behavior, OsmAnd visibility, startup policy, GPS freshness, trip-storage failover and focused transition CI.
- Added GDN visual palette, responsive large-head-unit dimensions and dedicated large-screen presentation styles without breaking the API25 regression floor.
- Latest fully verified implementation/UI baseline: `93891857d16945be7200e80fa5d24afd5e10c2cf`; GitHub Actions run `37982275776` / #367 **SUCCESS**.
- README, Current Status, Next Task and P10 review matrix aligned to GDN/Modern Android.
- Physical USB/GNSS/audio/ACC/CANBUS/OEM behavior and final display density/brightness/touch/thermal tuning remain **PENDING — PHYSICAL HEAD UNIT**.
- No firmware/MCU/kernel flashing, destructive root or generic BootReceiver/ACC behavior introduced.

## Historical record
The detailed pre-GDN changelog remains available through Git history and test-evidence records. Historical `Darbak` naming describes the product identity at the time those checkpoints were executed and is not the current production identity.

## 2026-10-02 — P6 Vehicle Data Foundation closed
- Added immutable Vehicle value/snapshot contract with provenance and freshness.
- Added optional source-adapter/store boundary for TPMS, OBD/CAN, fridge and later proven sources.
- Added dedicated truthful Vehicle UI consumption; unavailable/stale values are not shown as live.
- Corrected the existing shell navigation test for the dedicated Vehicle panel.
- Added focused API25 P6 gate; run 36978379715 PASS with VehicleDataTest 4/4, while preserving the P5 focused gate.
- Advanced project checkpoint to P7 Apps/Settings/Standby/alerts.
