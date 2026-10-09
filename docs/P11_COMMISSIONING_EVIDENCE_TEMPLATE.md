# P11 — Commissioning Evidence Template

Use one copy of this template per physical commissioning step. Do not mark a gate PASS from emulator/CI evidence.

## Session
- Date/time:
- Operator:
- Vehicle:
- Head-unit manufacturer/model:
- Android/API:
- Build fingerprint:
- SoC / RAM / storage:
- Native resolution / density:
- GDN APK/version/commit:

## Gate
- P11 step:
- Objective:
- Preconditions:
- Device state changed before test? YES / NO

## Procedure
1.
2.
3.

## Expected result
-

## Observed result
-

## Evidence
- Screenshot/photo:
- Log/file:
- Relevant package/component/build identifiers:
- Measured value(s):

## Result
- [ ] PASS
- [ ] FAIL
- [ ] BLOCKED

Reason / defect reference:
-

## Safety check
- Firmware flashed? NO
- MCU flashed? NO
- Kernel/boot modified? NO
- Destructive root/system change? NO
- OEM/system app removed or hidden? NO
- Generic BOOT_COMPLETED/guessed ACC logic added? NO

Any YES above requires a separately approved scope and must not occur before Recovery + Golden Backup readiness is proven.

## Next action
-

---

### Physical status rule
Until this template is completed against the exact production head unit, the gate remains:

**PENDING — PHYSICAL HEAD UNIT**
