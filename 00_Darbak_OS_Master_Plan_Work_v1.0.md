# Darbak OS — Master Plan for Work v1.1

## Mission
Build Darbak OS as a unified, modern in-car Android experience. Android remains the platform base; Darbak owns the normal day-to-day driving experience while external specialist engines such as OsmAnd remain behind explicit integration boundaries.

## Product target
- **Primary software target:** latest stable Android; current P10 baseline is Android 17 / API37.
- **Current build contract:** `compileSdk 37`, `targetSdk 37`, `minSdk 25`.
- **Production hardware:** modern replacement Android head unit; exact SoC/RAM/display/OEM/CANBUS profile remains UNKNOWN until the physical unit is selected and inspected.
- **Legacy regression floor:** Android 7.1 / API25. The former Allwinner T3 / ARMv7 / ~1 GB / 1024×600 platform is retained only as a lightweight regression floor and must not constrain modern architecture, UI or features.
- Arabic RTL first, offline-first and truthful state presentation remain permanent product requirements.

## Non-negotiable hardware safety gate
No firmware flash, MCU flash, kernel replacement, destructive root, destructive OEM removal/hiding, boot replacement or system-app removal before the exact production unit has:
1. Read-only hardware/system identity baseline.
2. Android/API/build/fingerprint/SoC/RAM/display identity.
3. OEM/CANBUS/ACC/autostart behavior recorded rather than assumed.
4. Partition/storage/recovery information appropriate to that unit.
5. Golden Backup as safely achievable.
6. SHA-256 verification of retained recovery artifacts.
7. A documented and tested recovery path.
8. Baseline functional tests.
MCU flashing remains prohibited until an explicit later decision backed by exact-device recovery evidence.

## Historical T3 policy
The retired t3-p3 / sun8iw11p1 / Allwinner T3 unit is not the production target. Existing T3/API25 evidence remains useful for regression and historical comparison only. Old T3 firmware candidates, ZH5/V8.3.2 research and DoFun references do not authorize firmware use on the future production device.

## Development/test path
Never use the production head unit as the first experiment target.

Current path:
`source/safety checks -> modern emulator/API37 -> focused compatibility tests -> modern visual/layout verification -> candidate APK -> exact production-head-unit commissioning -> hardware/OEM acceptance -> Stable`.

API25 CI remains a regression floor where it still provides value, but failures caused solely by retired-device visual/resource limits must not force the modern product backwards.

## Work checkpoint policy
Work in small, closed batches. Before moving to the next batch:
- complete the smallest useful unit;
- test it with the narrowest meaningful gate;
- commit/push it;
- update `01_CURRENT_STATUS.md`;
- update `02_NEXT_TASK.md`;
- record durable test/change evidence where relevant.
Never leave important project state only inside a chat session. GitHub is project memory and source of truth.

## Reuse-first policy
Before implementing a component from scratch, check `REFERENCES.md` and suitable proven upstream work. Verify license, Android/API compatibility, ABI requirements, performance cost and maintenance risk. Reuse concepts or bounded components rather than importing heavy projects without need. Record source, license, modifications and consumer when code is reused.

## User experience
Normal use must present Darbak, not a generic app-grid experience.

Primary areas:
- Home
- Map
- Media
- Vehicle
- Apps

Settings is secondary; technical Admin is hidden behind an intentional gesture. Emergency recovery remains independent.

Home is a driving dashboard, not a launcher grid. Visual priority is:
**speed -> navigation -> media -> vehicle state -> secondary quick actions**.

Normal state is quiet. Show actionable abnormalities rather than constant “OK” noise.

No core requirement for FM radio, weather, news, calendar or email.

## Modern UI / Visual Quality contract
The modern product must look and behave like software designed for a strong current-generation head unit, not an Android 7 / 1024×600 interface enlarged to Full HD.

Requirements:
- Full-HD-and-up responsive landscape composition without assuming one final screen size.
- Arabic RTL as the native layout direction.
- Strong hierarchy and glanceability for driving.
- Remove the heavy legacy top-bar feel.
- Lightweight/transparent lower navigation where appropriate.
- Modern cards/layers with restrained depth; avoid visual clutter and excessive blur.
- Day/night readability.
- Smooth, restrained motion/feedback that uses modern GPU capability without distracting the driver.
- Large, consistent touch targets suitable for an in-car display.
- No fabricated live data for visual effect.
- API25 compatibility is regression-only and may not downgrade the modern visual system.
- Final density, touch, brightness, thermal and animation tuning occurs on the physical production display.

## Core behavior
- OsmAnd is the offline map/navigation engine; Darbak wraps it through the lightest stable external boundary rather than rebuilding routing/maps.
- Darbak owns Position/Trip independently from OsmAnd.
- Trip Recorder is automatic once its approved runtime is active.
- Media never auto-plays after cold boot/wake; restore state only.
- Vehicle values always carry provenance/freshness; stale sensor values are never shown as live.
- GPS speed/location values have explicit freshness and expire to unavailable when updates stop.
- Standby is a calm Darbak UI state and does not silently stop trip recording.
- Voice, if implemented later, remains one-shot/explicit unless a future safety decision changes it.
- External/removable storage may be preferred for large data, but Darbak core remains functional with internal storage and must surface storage degradation truthfully.
- Loss of one secondary source must not collapse the overall experience.

## Startup / power boundary
Portable software may start continuous runtime only from an eligible user-visible path with required permissions. Generic `BOOT_COMPLETED`, ACC or OEM startup behavior is not assumed across Android head units.

Exact boot/wake/ACC/autostart integration belongs behind a hardware-specific adapter after the production unit is identified and recovery is proven.

Sudden power loss must be considered separately from normal shutdown. Trip persistence uses bounded chunks and synchronized commits; the active uncommitted buffer is a known exposure window documented in the P10 review.

## Architecture responsibilities
Core/Startup/Navigation/Common; Home; Maps integration; Media; Vehicle; Apps; Settings; Guardian; Diagnostics; Health; Update; Admin; Trip/Position; Data; Display/Touch; Audio; Input; Hardware abstraction; Storage/Backup; Connectivity.

Do not split into extra processes/APKs unless isolation benefit clearly justifies complexity and resource cost.

## Reliability
Guardian remains passive unless a later phase explicitly approves active recovery behavior. Preserve Known Good/Stable separately from TEST. Updates require compatibility inspection, integrity verification and a proven recovery/rollback path before destructive operations. A degraded external source is not Safe Mode. Never display stale navigation/TPMS/GPS/vehicle data as current.

## Data/storage
Maintain clear ownership of data, live state separate from history, safe chunked trip recording, rotating logs, schema/config migrations with rollback where needed, bounded retention and emergency internal fallback when removable storage fails. No Firebase/cloud dependency.

## Physical production-head-unit commissioning
Emulator success proves software compatibility only. It does not approve a real screen or head unit.

When the exact production device is available:
1. Collect read-only identity/baseline.
2. Run bounded Darbak APK smoke.
3. Verify display/touch/RTL/brightness and real performance.
4. Verify TPMS/ESP32 connectivity.
5. Verify physical GNSS and TripRuntime behavior.
6. Verify real audio routing and removable-media/USB behavior.
7. Verify real OsmAnd build/version and returned navigation information.
8. Measure CANBUS/OEM/ACC/boot/wake behavior.
9. Establish and verify recovery/Golden Backup/hash evidence.
10. Only after those gates consider deeper OEM/autostart integration.

## Phase map
- **P0** Repository/process baseline — CLOSED.
- **P1** Development/test environment and initial shell — CLOSED in its historical scope.
- **P2** Home/navigation shell and design foundation — CLOSED in its historical scope.
- **P3** Core state/services + Guardian foundation — CLOSED.
- **P4** OsmAnd boundary + Position/Trip — CLOSED in software scope.
- **P5** Media — CLOSED in software scope.
- **P6** Vehicle data foundation — CLOSED in software scope.
- **P7** Apps/Settings/Standby/alerts — CLOSED in software scope.
- **P8** Update/Admin/Recovery readiness — CLOSED in software scope.
- **P9** Modern head-unit software readiness — PASS; physical commissioning PENDING.
- **P10** Android-modern compatibility + Modern UI + integration boundaries — ACTIVE. API37 compatibility review PASS; Modern UI / Visual Quality is the next software gate; OEM/ACC/USB/GNSS/audio/CANBUS physical integration remains pending exact hardware.
- **P11** Stable acceptance — after P10 and physical commissioning.

## P10 evidence
Current review matrix: `docs/P10_ANDROID_MODERN_REVIEW.md`.

Verified compatibility run: GitHub Actions `37667936775` on implementation head `77da6df90bbfffc869a927ea39673ef9220c4cf2` — SUCCESS, with API37 Build/Lint, API25 regression floor and the focused Android-modern review gate all passing.

## Stop conditions
Stop and document rather than assume when:
- a task requires firmware/MCU/kernel/destructive-root action before recovery is proven;
- exact OEM/ACC/CANBUS behavior is unknown;
- a physical claim cannot be established in emulator;
- a dependency has unacceptable license/security/performance cost;
- a test failure is infrastructure/test-harness related rather than production-code related—fix the harness and preserve the production contract.

## Work instruction
Start from `02_NEXT_TASK.md`. Do not reopen closed decisions without test evidence of a conflict. Finish, test, commit and checkpoint each batch so another session can continue immediately.
