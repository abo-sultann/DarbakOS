# 02_NEXT_TASK — P10 Modern UI / Visual Quality

Android-modern compatibility is now green on Android 17 / API37. Do **not** repeat broad compatibility work unless a concrete regression appears.

Verified baseline:
- Branch `p10-review-fixes-20261006`.
- CI run `37667936775`: SUCCESS.
- API37 Build/Lint: PASS.
- API25 legacy regression floor: PASS.
- P10 modernization review items 1–7: software fixes/tests PASS within their stated scope.
- Review matrix: `docs/P10_ANDROID_MODERN_REVIEW.md`.

## Next software task

Modernize Darbak OS visually for a strong current-generation Android head unit while remaining hardware-agnostic until the exact production screen is known.

### Required outcomes
1. Rework the Home information architecture around driving priority: **speed → navigation → media → vehicle state**.
2. Remove the heavy legacy top-bar presentation and avoid the appearance of a 1024×600 Android 7 UI scaled up to Full HD.
3. Build a responsive landscape layout suitable for Full HD and above, with density-safe sizing instead of hard-coding one future screen.
4. Treat Arabic RTL as the native design direction.
5. Use a clean automotive visual system: large high-value information, restrained cards/layers, clear state hierarchy, lightweight/transparent lower navigation where appropriate.
6. Add day/night readability foundations and restrained modern motion/feedback without introducing distracting animation.
7. Preserve truthful unavailable states and current functional contracts; no fabricated live data.
8. Keep API25 as a regression floor only. Do not degrade the modern UI to satisfy the retired T3 hardware.
9. Add focused modern visual/navigation tests and capture modern emulator screenshots for review. Emulator screenshots validate software layout only, not physical-screen approval.

## Hardware-specific work remains deferred

When the exact production head unit is selected/available:
1. Record exact Android/API/SoC/RAM/display/build/OEM/CANBUS identity using the read-only baseline collector.
2. Run bounded Darbak APK smoke.
3. Verify real USB mount/unmount/remount, TPMS/ESP32, GNSS/TripRuntime, audio routing and real OsmAnd.
4. Measure ACC/boot/wake/autostart behavior before creating any OEM startup adapter.
5. Verify recovery path and Golden Backup/hash before any destructive platform change.
6. Only then evaluate deeper OEM/CANBUS/autostart integration.

No generic BootReceiver, firmware/MCU/kernel flashing, destructive root, OEM hiding, boot replacement or system-app removal is approved at this stage.
