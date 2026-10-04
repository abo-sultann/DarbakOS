# 02_NEXT_TASK — P9 Physical Production Head Unit Commissioning

Software readiness is complete. Do not repeat or expand emulator work without a concrete regression.

When the exact replacement head unit is selected/available:
1. Record exact hardware/Android/API/SoC/RAM/display/build/OEM/CANBUS identity using the read-only P9 production-device baseline collector.
2. Run a bounded Darbak APK smoke on the physical unit.
3. Verify TPMS/ESP32 connectivity, GPS/TripRuntime, Media/OsmAnd, then OEM/CANBUS behavior.
4. Record the recovery path and create/verify the Golden Backup with SHA-256 before any destructive platform change.
5. Keep firmware/MCU/kernel flashing, destructive root, OEM hiding, boot replacement, and system-app removal locked until recovery is proven.

P9 remains OPEN until physical commissioning evidence is recorded. P10 OEM/CANBUS/autostart integration follows the exact hardware profile.


## P10 preflight finding — defer until exact production hardware
- The current `TripRuntimeService` remains the legacy API25-compatible sticky service and is intentionally unchanged during P9.
- On a modern target, continuous GPS must be commissioned against the exact device/autostart path using the Android foreground-service location contract rather than assuming legacy `START_STICKY` behavior.
- Android 15 background/BOOT_COMPLETED foreground-service restrictions make OEM/ACC startup behavior part of the physical-device acceptance gate.
- Do not add a generic boot receiver or force a modern foreground-service migration until the exact head unit's OEM launcher/ACC/autostart behavior is measured.
- Media startup must likewise be verified against the exact target before any boot-time media service is introduced.
