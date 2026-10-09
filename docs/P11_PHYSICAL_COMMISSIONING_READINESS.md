# P11 — Physical Commissioning Readiness

**Status:** READY TO START WHEN THE PRODUCTION HEAD UNIT IS PHYSICALLY AVAILABLE  
**Software baseline:** `main` after P10 merge commit `1e2cc0185653ea9dcacabb51d9128857f143befa`  
**Post-merge CI:** run `38000534974` / #374 — **SUCCESS**

## Purpose
P11 is the controlled transition from emulator/software acceptance to the exact production head unit. It does not reopen P10 unless a real regression is discovered.

All checks in this document are **PENDING — PHYSICAL HEAD UNIT** until measured on the actual unit.

## Hard safety boundary
Before Recovery/Golden Backup is proven:
- no firmware flashing;
- no MCU flashing;
- no kernel/boot image changes;
- no destructive root/system modification;
- no system-app deletion/hiding;
- no generic BOOT_COMPLETED receiver or guessed ACC integration;
- no replacement of OEM launcher/startup behavior based on assumptions.

## Prepared tooling
- `scripts/p11_readonly_baseline.sh` — read-only ADB collector for the first commissioning gate. It records device/build, Android/API, SoC/ABI, RAM/storage, display, USB, GNSS/location, audio/media, power/process state and package evidence without install, settings writes, reboot, root, remount or flashing.
- `docs/P11_COMMISSIONING_EVIDENCE_TEMPLATE.md` — standard evidence record for every physical gate.

The baseline collector is preparation only. Its output is not a PASS until reviewed against the exact production unit.

## Commissioning order

### 1. Read-only device baseline
Record without changing the unit:
- manufacturer/model/build fingerprint;
- Android version/API level/security patch;
- SoC/ABI/GPU/RAM/storage;
- physical resolution, density and refresh rate;
- USB topology/storage exposure;
- GPS/GNSS provider state;
- audio devices/routing visible to Android;
- OEM/MCU/CANBUS identifiers exposed by Settings/system properties where safe.

Preferred first capture from a connected authorized ADB host:
`bash scripts/p11_readonly_baseline.sh`

**Exit condition:** exact unit identity is captured and attached to project evidence.

### 2. Bounded GDN APK smoke
Install only the known P10-approved APK and verify:
- launch/return/relaunch;
- Arabic RTL;
- Home layout fit;
- speed/navigation/media/vehicle unavailable states remain truthful;
- no crash/ANR/black screen;
- app can be removed/reinstalled cleanly.

**Exit condition:** basic UI/runtime is stable before any integration work.

### 3. Display and touch acceptance
Measure:
- effective density and scale;
- clipping/overlap at native resolution;
- touch target usability while stationary;
- day/night readability;
- brightness range;
- animation smoothness and thermal behavior.

Do not tune global dimensions until the exact measurements are recorded.

### 4. TPMS / ESP32 connectivity
Verify the intended GDN/TPMS link on the real unit without changing TPMS firmware unless a proven interface issue requires it.

Record:
- connection method;
- reconnect behavior after sleep/restart;
- latency/stability;
- whether Android/OEM power management interferes.

### 5. GNSS / TripRuntime
Verify:
- precise location permission flow;
- first-fix time;
- live speed freshness;
- stale-fix expiry;
- background continuity while OsmAnd is foregrounded;
- stop/restart behavior;
- trip persistence.

No OEM autostart/ACC adapter is added during this step.

### 6. Media / USB / OsmAnd
Verify real hardware behavior:
- USB insert → discover audio;
- unplug → stale URI/media disappears safely;
- reinsert → rediscovery;
- playback/resume/audio focus;
- actual audio routing through head-unit amplifier;
- installed production OsmAnd package visibility, launch, return and navigation handoff.

### 7. CANBUS / OEM integration discovery
Read and document first. Identify:
- CANBUS box/vendor/app/package if present;
- steering-wheel controls path;
- reverse/camera behavior;
- OEM settings bridge;
- sleep/wake/ACC semantics.

Do not generalize behavior from another head unit.

### 8. ACC / boot / wake characterization
Observe repeated cycles before coding:
- cold boot;
- ACC off/on;
- short sleep;
- long sleep;
- reboot;
- process retention/kill;
- whether Android launcher/app state is restored by OEM software.

Only after evidence exists may a hardware-specific startup adapter be designed behind `StartupCoordinator`.

### 9. Recovery readiness
Identify and prove the exact recovery path for this unit:
- stock recovery/factory restore availability;
- firmware source authenticity;
- partition/backup options;
- non-destructive restore test if possible.

### 10. Golden Backup
If the unit permits a trustworthy backup:
- capture before destructive changes;
- hash every backup artifact;
- record unit identity/build against the backup;
- store restoration instructions separately from experimental changes.

Only after Recovery + Golden Backup are verified can deeper platform modification be considered.

## Evidence required per step
For every physical gate record:
- date/time;
- exact device/build identity;
- action performed;
- expected result;
- observed result;
- PASS / FAIL / BLOCKED;
- screenshots/logs where useful;
- whether any device state was changed.

## P11 exit rule
P11 commissioning is complete only when the exact production unit passes the bounded hardware gates required for normal GDN use and every unsupported/OEM-specific item is documented explicitly.

Physical success never authorizes destructive firmware/MCU/root work by itself; such work requires a separate reviewed scope after Recovery/Golden Backup readiness is proven.
