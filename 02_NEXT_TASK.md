# 02_NEXT_TASK — Post-P10 / Physical Commissioning Readiness

P10 software modernization is **closed and merged to `main`**. Do **not** reopen broad Android-modern compatibility, identity cleanup or UI-foundation work unless a concrete regression appears.

## Verified baseline
- P10 reviewed branch head: `855731e4ce23c5cdea117c552ac449703921a8f0`.
- Merge commit on `main`: `1e2cc0185653ea9dcacabb51d9128857f143befa`.
- Pre-merge CI run `37993383810` / #373: **SUCCESS**.
- Post-merge `main` CI run `38000534974` / #374: **SUCCESS**.
- Android 17 / API37 Production Target: PASS.
- API25 Legacy Regression Floor: PASS.
- Product identity: **غضن | GDN**.

## Next approved work
Until the replacement production head unit is physically available, keep software changes bounded to proven defects or clearly approved standalone features. Do not invent hardware assumptions.

When the production head unit arrives, execute commissioning in this order:
1. **Read-only baseline:** Android/API, build fingerprint, SoC, RAM/storage, display resolution/density, USB topology, OEM/MCU/CANBUS identity where observable without modification.
2. **Bounded GDN APK smoke:** install/launch/Home/RTL/navigation between internal surfaces; capture logs and exact version/build identity.
3. **TPMS/ESP32:** verify real connectivity and background behavior without changing firmware assumptions.
4. **GNSS/TripRuntime:** real fixes, stale behavior, background continuity and storage state.
5. **Media/OsmAnd:** USB mount/unmount/remount, audio routing, MediaStore visibility and real OsmAnd handoff/return.
6. **CANBUS/OEM:** observe real available signals/functions before implementing adapters.
7. **ACC/boot/wake:** measure actual behavior; do not add a generic receiver.
8. **Recovery readiness:** prove Recovery path and create/verify Golden Backup/hash where possible.
9. Only after step 8 may deeper OEM/startup/platform integration be evaluated.

Everything in steps 1–9 is currently **PENDING — PHYSICAL HEAD UNIT**.

## Explicitly not approved yet
- firmware/MCU/kernel flashing;
- destructive root changes;
- boot replacement or OEM app removal;
- generic BOOT_COMPLETED/ACC startup logic;
- namespace/applicationId migration;
- persisted-key renaming without a migration plan and regression coverage.

## P11
P11 is **not started**. Define it only when there is a concrete post-P10 objective or physical commissioning evidence that justifies a new software phase.
