# Current Status

**Updated:** 2026-10-07  
**Authority:** GitHub repository state and verified CI evidence.

## Phase state

- **P0–P8:** CLOSED within their documented software scopes.
- **P9:** SOFTWARE READINESS PASS / PHYSICAL COMMISSIONING PENDING.
- **P10:** ACTIVE.
  - Android-modern compatibility review: **PASS on Android 17 / API37**.
  - Modern UI / Visual Quality: **NEXT SOFTWARE GATE — NOT CLOSED YET**.
  - OEM/ACC/boot/USB/GNSS/audio/CANBUS physical integration: **PENDING EXACT PRODUCTION HEAD UNIT**.
- **P11:** Not started; stable acceptance follows physical commissioning and P10 closure.

## Authoritative target

- Primary software target: **Android 17 / API 37**.
- App build: `compileSdk 37`, `targetSdk 37`, `minSdk 25`.
- Android 7.1 / API25 remains a regression floor only.
- The former Allwinner T3 / t3-p3 / sun8iw11p1 / ARMv7 / ~1 GB / 1024×600 unit is retired from production-car use and is not allowed to constrain the modern product UI or architecture.
- Exact production head-unit SoC/RAM/display/OEM/CANBUS/ACC behavior remains intentionally unset until physical identification.

## P10 Android-modern compatibility — PASS

Working branch: `p10-review-fixes-20261006`  
Draft PR: #1 — `P10: Android 17 migration + review gate`  
Verified CI run: `37667936775`  
Verified implementation head: `77da6df90bbfffc869a927ea39673ef9220c4cf2`  
Artifact: `11504071154`  
Result: **SUCCESS**.

The verified run passed:
- source/safety checks;
- Android 17/API37 SDK provisioning;
- API37 Build + Lint;
- Android 7/API25 legacy regression floor;
- Android 17 emulator startup at modern landscape profile;
- clean-install real system location-permission flow;
- real `READ_MEDIA_AUDIO` permission flow;
- MediaStore shared-audio discovery contract;
- OsmAnd absent/present visibility fixture checks;
- foreground trip-runtime continuity while an external OsmAnd fixture is foregrounded and after return;
- `RuntimeStartPolicy` and `StartupCoordinator` tests explicitly invoked by CI;
- stale GPS-position expiry;
- trip-storage failover and status reporting;
- preserved P4/P5 portable service regression tests.

Full review matrix: `docs/P10_ANDROID_MODERN_REVIEW.md`.

## P10 review disposition

1. **Location permissions — fixed and tested.** Coarse+Fine are requested together; Approximate/Precise/Denied are distinguished; portable trip runtime starts only when precise location and eligible foreground conditions exist. Clean-install Android 17 system UI is tested without ADB pre-grant for the proof path.
2. **Audio/shared media — fixed in software; physical USB pending.** Modern Android uses `READ_MEDIA_AUDIO` and MediaStore; legacy external-read permission is bounded to API<=32. URI-backed playback is supported and Play/Pause resumes the same `MediaPlayer` position.
3. **Apps/OsmAnd visibility — fixed/tested against a controlled fixture.** Manifest package visibility is explicit. Real production OsmAnd build remains a commissioning check.
4. **GPS/trip startup — fixed for portable user-launch path.** Startup is centralized through `StartupCoordinator`. No generic BootReceiver/ACC assumption was introduced.
5. **Stale speed — fixed/tested.** Position freshness expires after the policy timeout and the UI receives unavailable state rather than presenting an old speed as live.
6. **Trip-storage failure — fixed in software.** External write failure can fall back to internal storage with pending-point replay and visible state. The active uncommitted chunk is at most 60 position fixes; at nominal 1 Hz this is approximately up to 60 seconds of sudden-power-loss exposure.
7. **Transition CI — fixed and running.** The focused modernization tests are now explicit workflow commands rather than merely existing test files.
8. **Documentation — being aligned in the current documentation batch.** Historical evidence remains in `TEST_RESULTS.md`; current status and next work are defined here and in the P10 review document.

## Modern UI / Visual Quality — NEXT

Compatibility success does **not** close P10. The next software batch modernizes the visible product so it reflects the capability of a strong modern screen rather than an enlarged Android 7 shell.

Acceptance direction:
- Full-HD-and-up adaptive landscape layout.
- Arabic RTL as a first-class design.
- Strong visual priority for speed, navigation, media and vehicle state.
- Remove the heavy legacy top-bar feel.
- Cleaner automotive cards/layers and a lightweight/transparent lower navigation treatment.
- Day/night driving readability.
- Restrained smooth motion and modern GPU effects only where they improve hierarchy/feedback.
- API25 remains regression-only and must not downgrade the modern visual system.

Exact density, touch targets, brightness, animation/thermal tuning and physical readability remain final-device commissioning items.

## Physical commissioning still pending

Do not infer any of the following from emulator success:
- exact screen fit/density/brightness/thermals;
- real USB mount/unmount/remount behavior;
- physical GNSS behavior;
- real audio routing/amplifier behavior;
- ACC/boot/wake/autostart semantics;
- CANBUS/OEM integration;
- Golden Backup or recovery readiness for the production unit.

Before destructive platform work: exact-device baseline → bounded APK smoke → TPMS/ESP32 → GPS → Media/OsmAnd → CANBUS/OEM → verified recovery/Golden Backup → only then evaluate deeper startup/OEM integration.

GitHub remains the project-state authority. Detailed historical test evidence remains in `TEST_RESULTS.md` and `docs/test-evidence/`.
