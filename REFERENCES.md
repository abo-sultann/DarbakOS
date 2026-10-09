# غضن | GDN — References

Permanent reuse log. Before building from scratch, check these sources and inspect the exact upstream version. When code is reused, record source commit/tag, license, GDN destination, modifications and API25/ARMv7/resource test result.

| Reference | URL | Use in GDN | Type | Compatibility / rule |
|---|---|---|---|---|
| OsmAnd | https://github.com/osmandapp/OsmAnd | Offline maps/search/navigation/favorites/routing integration | Engine/API/reference | Primary map engine. Prefer AIDL/API/wrapper; avoid unnecessary core fork. Pin an API25/ARMv7-compatible version after testing. |
| Open Launcher | https://github.com/dw2lam/openlauncher | Offline-first/OEM+ launcher ideas, GPS/dashboard/day-night patterns | Concept/reference | Inspect small reusable patterns only; GDN keeps fixed lightweight automotive cards rather than importing its full widget system. |
| Dashline | https://github.com/metehankaygsz/dashline | Old/weak Android head-unit implementation patterns, classic Views, media/app shortcuts, adaptive cards | High-priority code/reference candidate | Runs on Android 4.4+ and no Play Services. Current project is GPLv3; code reuse requires license review before copying into GDN. Concepts may be reimplemented independently. |
| Femto Car Launcher | https://github.com/seijikohara/femto-car-launcher | Stable/nightly separation, test discipline, glanceable UI, failure/backoff ideas | Concept/reference | Requires Android 13/API33; do not import its modern stack into T3/API25. |
| Helm head-unit platform | https://github.com/HelmMobile/helm | Launcher/hardware/MCU separation ideas | Architecture concept | Newer hardware; ideas only until individually verified. |
| LibAuto | https://github.com/f1xpl/LibAuto | Car-state/night/GPS/key channel concepts | Concept/reference | Inspect exact code/license/Android requirements before reuse. |
| OpenMasjidKiosk | Search/verify upstream before code reuse | Kiosk/HOME + hidden admin gesture concept | Concept/reference | Pattern only until exact upstream/license is logged. |
| BMW iDrive Launcher references | Search/verify exact upstream before code reuse | Crash/ANR/black-screen monitoring, staged recovery | Concept/reference | Reimplement for API25 unless an exact compatible source is verified. |
| Minimal Car Launcher | Search/verify exact upstream before code reuse | Lightweight GPS/quick-card/startup concepts | Concept/reference | Do not copy until exact upstream/license is recorded. |
| KSW Car Project | Search/verify exact upstream before code reuse | MCU communicator/EventCenter patterns | Architecture concept | Reference only. |
| CarRadio / TWUtil / TWClient references | Search/verify exact upstream before code reuse | Evidence/reference for T3 vendor APIs | Platform reference | FM radio excluded. Investigate only non-radio T3 functions useful to GDN. |
| 4PDA/XDA Allwinner T3 material | Community sources | SWC, sleep/ACC, factory settings, USB, MCU mismatch evidence | Community reference | Device-specific/anecdotal; never generalize without exact-unit validation. |
| DoFun / T3 Firmware channels | Telegram/community sources | Recovery/tools/APKs/boot-animation/TS-T3 ideas | Tool/reference | No DoFun firmware approved for this unit. |
| t3-p3 Android 7.1.1 dumps | External reference dumps | Partition/file/build comparison | Comparison only | NEVER substitute for this unit's Golden Backup. |
| Breadcrumb/Colota/OSMTracker-style trip projects | Upstreams to be pinned when used | Continuation, event waypoints, merge/split ideas | Concept/reference | Concepts only until fit/license review. |

## Reference selection order
1. Existing Android/API25 capability.
2. OsmAnd capability when map/navigation/location-related.
3. A proven lightweight API25-compatible component/reference.
4. Adapt a small upstream component.
5. Implement from scratch only when the above do not fit.

## Mandatory code-reuse record
For every code-level reuse append:
- Source repository URL
- Commit/tag
- Source file/component
- License
- GDN destination
- Why reuse is preferable
- Changes made
- minSdk/API/ABI/dependency check
- RAM/performance observations where applicable
- Test result on laptop/Test Station/T3 as stages become available

## Rules
- "Available" is not "suitable."
- No dependency may silently raise minSdk above API25.
- Reject unsupported ABI/native libraries or excessive RAM/GPU cost.
- Do not duplicate stable OsmAnd/Android capabilities.
- Never import a full launcher simply to obtain one useful component.
- Concept inspiration does not justify copying code; code-level reuse requires an exact source and license check.

## P1 reuse review — 2026-09-27
Reviewed before implementing the shell; no map/hardware integration is in this batch.

| Source/version | Inspected component/license | Decision and Darbak destination | Compatibility |
|---|---|---|---|
| Android platform Views/API25 | Activity, LinearLayout, TextView, Button, drawable resources; Android SDK APIs | Use existing framework layout, focus, saved-instance state and RTL capabilities. No UI runtime library or custom rendering engine. | minSdk 25; Java 8 bytecode; no native libraries. Runtime results go in TEST_RESULTS.md. |
| [Launcher.2026](https://github.com/abo-sultann/Launcher.2026/tree/eafa48965c501b0297f33d9a23556ab9a04a41da) and [DarbakLauncher.v2](https://github.com/abo-sultann/DarbakLauncher.v2/tree/d99834deb6f60a3538f0ce1a8030ec4719fdde58) | app/build.gradle.kts; Launcher ui/theme/Color.kt. Owner's repositories; no standalone license found. | Reuse the owner's color values in app/src/main/res/values/colors.xml (XML conversion and descriptive token names). Do not copy Compose, mapsforge, signing configuration or updater. | Existing minSdk 25, but their full Compose/maps stack is unnecessary for this shell. No third-party implementation copied. |
| [DarbakTestStation](https://github.com/abo-sultann/DarbakTestStation/tree/a9cbbcf80d90edf1276290e1fc0651e0d3fdd562) | app/build.gradle, styles.xml, Android build workflow; owner's repository, no standalone license found | Concept: plain Android/Java, no runtime dependencies, separate test APK. Independently configure this project's build. | Existing minSdk 25; its gold/light palette is not substituted for Launcher palette. |
| [Dashline](https://github.com/metehankaygsz/dashline/tree/be4c98fcc649abdec55e687c5e7cda02d9c73001) | README, app/build.gradle.kts, LICENSE (GPLv3) | Concept only: landscape cards and classic Views. No GPL code/assets copied, no weather/radio/widgets imported. | Upstream legacy minSdk 19, Java 8, AndroidX Views; no need to import these dependencies for P1. |
| [Gradle v8.9.0](https://github.com/gradle/gradle/tree/v8.9.0) | gradlew, gradlew.bat, gradle/wrapper/gradle-wrapper.jar; Apache-2.0 | Unmodified official wrapper scripts/JAR copied to the same paths. License retained in third_party/gradle-LICENSE.txt. Project wrapper properties pin Gradle 8.9. | Build-time only, JDK17. No APK/ABI/RAM cost. |
| [Android Gradle Plugin 8.7](https://developer.android.com/build/releases/agp-8-7-0-release-notes) | Official compatibility table | Pin AGP 8.7.3, Gradle 8.9, JDK17; compileSdk35/build-tools34.0.0. | Compilation SDK does not raise minSdk25; targetSdk25 is a P1 test baseline, not a Play Store release. |
| [Android Emulator Runner v2](https://github.com/ReactiveCircus/android-emulator-runner) | Official README/action inputs; MIT | CI action only, not vendored. Use API25/x86 1024x600/160dpi, software GPU, 1GB RAM. | x86 emulator validates Android behavior/layout, not ARMv7 performance or real T3 acceptance. |

No API33-only Femto stack, firmware references, or external launcher modules are needed for P1. APK size and runtime observations must be measured, not inferred from the reference projects.

P1 CI correction: use [android-actions/setup-android v3](https://github.com/android-actions/setup-android/tree/v3) (MIT, build-time action only) to provision command-line tools after the first runner proved sdkmanager was absent. No app dependency added.

P1 measured result at `72fde4a`: runtime APK 18,221 bytes with no native libraries; 8,636KB PSS in API25/x86 emulator; build, 4 tests, RTL fit and navigation passed. CPU unavailable; ARMv7/T3/TestStation remain untested. See TEST_RESULTS for limitations. No Dashline code was copied.

## P3 closure CI reuse — 2026-09-30
- Official `actions/download-artifact` v4, pinned commit `d3f86a106a0bac45b974a628896c90dbdf5c8093`, MIT; README/LICENSE inspected. Used only in `.github/workflows/p1-android.yml` to retrieve the already verified diagnostics APKs/lint instead of rebuilding unchanged source. No source code copied or app dependency introduced; GitHub-hosted CI only, no API25/ABI/RAM impact.
- Existing emulator smoke and Guardian proof conventions reused.52 build-input SHA256 checks and artifact hashes gate reuse; earlier exhaustive/concurrency evidence remains pinned. Android results recorded in TEST_RESULTS.md.

## P4 OsmAnd bridge reference — 2026-09-30
- Upstream inspected at `osmandapp/OsmAnd@26e32fb929b18cc6f6614855f184f5627a1fc7af`.
- `OsmAnd/build.gradle` confirms official application IDs used by current flavors: `net.osmand.plus` (full/androidFull), `net.osmand` (gplayFree), and `net.osmand.dev` (nightlyFree). Darbak uses only these identifiers for conservative capability detection.
- `OsmAnd-api/src/net/osmand/aidlapi/IOsmAndAidlInterface.aidl` confirms the maintained API surface includes map location, GPX, route calculation, navigation/search/customization and callbacks. This first bridge does **not** copy/vendor that interface; it uses Android PackageManager/Intent only and reserves AIDL for the next compatibility slice.
- Upstream repository code license is GPLv3; artwork is CC-BY-NC-ND 4.0 with noted exceptions. No OsmAnd GPL source or artwork was copied into Darbak in this bridge batch.
- Darbak destination: `OsmAndPackages` + `OsmAndBridge`. Implementation is independent Android framework code, no runtime dependency, no native library and no minSdk increase. Physical API25/ARMv7/T3 compatibility of an installed OsmAnd APK still requires a later device/version test.

## P4 reuse review — 2026-09-30

| Source/version | Inspected capability | Decision for Darbak | Compatibility / license |
|---|---|---|---|
| OsmAnd `7042e1764309841897b94c578011393af536e04f` | Official Android AIDL API and intent boundary; map markers/location/navigation callbacks are exposed through the separate OsmAnd app | Keep OsmAnd as the navigation engine. Darbak owns GPS/trip history; use intents now and add only the narrow AIDL surface needed for navigation state instead of embedding/forking OsmAnd | Official upstream; GPL project. No OsmAnd source copied in this batch, so no new runtime dependency/ABI/RAM cost |
| OSMTracker Android `0c32781db83ea7642093793d00580b2953b6ea13` | GPS track/GPX recording and waypoint model | Concept/reference only. Its current fork targets minSdk25, but importing the GPL application/storage stack is unnecessary because Darbak already has a smaller API25 recorder | GPL-3.0; no code copied |
| Breadcrumb `4d022a5b54a76bef956ebb867e0769de79bfc61d` | Automatic movement-based trip lifecycle, GPS-only measured fixes, local history, gap/continuation ideas | Reuse concepts only: movement starts/stops recording, keep GPS independent of map UI, preserve gaps rather than inventing a path. Reject its full modern stack for T3 | Modern Kotlin/Compose/Room + Play Services Activity Recognition + MapLibre; unsuitable as a dependency for Android7.1/~1GB. No code copied |

### P4 integration decision
The lightweight Darbak implementation remains preferable to importing either trip application. Continuous recording is moved out of MainActivity into an API25 Service with a dedicated HandlerThread: GPS callbacks and chunk persistence continue while OsmAnd is foreground and disk I/O does not block the Darbak UI. PositionStore is the process-local handoff back to Home. This is an independent implementation based on platform APIs and recorded concepts, not copied upstream code.

### Continuous-runtime verification references
- Android platform [Service lifecycle/threading](https://developer.android.com/develop/background-work/services) and [HandlerThread](https://developer.android.com/reference/android/os/HandlerThread) documentation reviewed for lifecycle callbacks, worker ownership and orderly shutdown. API25 platform behavior is exercised by instrumentation; no library or upstream implementation copied.
- Reused the repository's P4 GPS smoke, existing ActivityScenario assertions, PositionQualityPolicy, buffer/write-first persistence and strict chunk reader. New test-only LocationManager fixtures exercise the actual worker-to-Home handoff; they introduce no production mock provider or permission. Existing owner-project/reuse decisions above remain unchanged.

## P4 OsmAnd External API + final Map decision — 2026-09-30
- Re-inspected current OsmAnd external integration at upstream `osmandapp/OsmAnd@b9d4b9959f1a2121e2c5deee63587e79ad460d2c` before adding another dependency.
- `OsmAnd/src/net/osmand/plus/helpers/ExternalApiHelper.java` exposes the documented `osmand.api` commands needed by this slice: `navigate_search`, `show_location`, and `get_info`. `get_info` returns destination, ETA, remaining time/distance and current/next turn fields when a route is calculated. The OsmAnd manifest exports the `osmand.api` VIEW intent filter.
- This makes a copied/generated AIDL client unnecessary for the first final Map integration. Darbak now prefers the official external URI API for search/location and one-shot route snapshots; AIDL remains a capability probe and can be added later only if physical T3/OsmAnd validation proves live callbacks are required.
- Reason: upstream is GPLv3. By using Android intents and documented field names only, Darbak copies no OsmAnd source/AIDL/artwork, adds no runtime library/native ABI/minSdk cost, and keeps the OsmAnd app as the independent offline navigation engine.
- Darbak implementation: `OsmAndBridge` + `OsmAndNavigationSnapshot` + final Map controls in `MainActivity/activity_main.xml`. Darbak remains owner of GPS/trip recording; OsmAnd owns map/search/routing. Route state is never fabricated: absent API = unavailable, no returned route = idle, returned route = active, and old snapshots become stale.
- Current emulator can prove fallback/absent behavior and parser/UI correctness only. Installed OsmAnd command execution and ARMv7/T3 performance remain a physical/version compatibility gate.
