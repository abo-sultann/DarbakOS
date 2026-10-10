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
Historical `Darbak` naming below describes the product identity at the time those checkpoints were executed and is preserved as the authoritative project history.

## 2026-10-02 — P6 Vehicle Data Foundation closed
- Added immutable Vehicle value/snapshot contract with provenance and freshness.
- Added optional source-adapter/store boundary for TPMS, OBD/CAN, fridge and later proven sources.
- Added dedicated truthful Vehicle UI consumption; unavailable/stale values are not shown as live.
- Corrected the existing shell navigation test for the dedicated Vehicle panel.
- Added focused API25 P6 gate; run 36978379715 PASS with VehicleDataTest 4/4, while preserving the P5 focused gate.
- Advanced project checkpoint to P7 Apps/Settings/Standby/alerts.


## 2026-10-01 — P5 External MediaSession focused gate repaired / PASS / STOP
- Diagnosed requested run36817089400: two literal backslash-n sequences prevented the Python runner from starting. After repairing syntax, run36817725413 exposed a second runner-only defect: singular JUnit success was rejected despite passing9 +1 tests.
- Corrected only `scripts/p5_media_smoke.py`; test selection/counts/assertions, fixture, app, workflow and P4/OsmAnd are unchanged.
- Final run36818005050 at7c4bf543: Build/Lint0 errors/17 warnings; same API25/1024x600 focused gate11/11 PASS. External paused-session observation/no autoplay, explicit Play/Next, Home updates and granted-access idle state passed.
- Saved both failure stages and final raw evidence, reviewed Home screenshots, verified empty crash buffer/no app ANR and89 unchanged input fingerprints. APK size unchanged at71,475 bytes; ZIP-entry payloads identical to incoming.
- Updated current status and replaced stale P4 task with completed P5 gate/STOP. No Full Regression, Guardian suite, separate P4/OsmAnd suite or next phase. P5 remains open beyond this foundation.

## 2026-09-30 — P4 Continuous GPS + Automatic Trip Runtime verified / STOP
- Proved runtime/recorder defects with a10-test API25 probe (9 failing assertions), then corrected only TripRuntimeService, TripAutoRecorder and MainActivity. Preserved before-fix evidence and original assertions.
- Moved storage/close onto the Service worker, drained queued points before shutdown, cleared stopped availability without invalidating replacement ownership, and preserved PositionStore across fresh Home creation.
- Automatic recording now rejects noncredible/order-breaking inputs, separates long GPS gaps and retries a failed full chunk before accepting a new point. Reused the existing persistence format/components.
- Build/Lint PASS (0 errors,17 warnings); one consolidated focused API25/1024x600 gate26/26 PASS. Actual GPS/Home/background handoff and persisted background point PASS; provider-disabled unavailable state PASS. No Full Regression or Guardian suite.
- Updated the source guard for the explicitly approved private Service/worker. No new permission/runtime dependency/native library or layout change. Saved reviewed Home screenshots, receiver/thread/storage proof, resource observations and durable provenance. P4 remains open; STOP without AIDL/Map UX work.
## 2026-09-30 — P4 Trip Recording Foundation focused check complete / STOP
- Fixed one proven ordering defect: an empty new segment accepted older/duplicate timestamps after pause/resume or finish/start. Retained four-case before/after reproduction; reset clears the new last-accepted timestamp.
- Preserved supplied assertions and added three focused TripRecorder tests for cross-segment order/sequence, quality/gap boundaries and immutable snapshot/reset behavior.
- Build/Lint PASS (0 errors,17 warnings); exactly one API25/1024x600 focused run: PositionStateTest2/2 + TripRecorderTest6/6 =8/8 PASS. No regression, Guardian, GPS/OsmAnd/persistence or UI smoke run.
- Reused the existing focused CI runner, saved raw evidence/provenance and updated status. Existing GPS permission/provider, persistence and intent bridge predate this bundle; no new background/I/O/dependency path was introduced. APK50,855 bytes, no observed crash/app ANR. P4 remains open; STOP.

## 2026-09-30 — P4 Position Foundation focused check complete / STOP
- Build/Lint PASS (0 errors,17 unchanged warnings), PositionStateTest only2/2 PASS on API25/1024x600. No proven production defect or app/test correction needed.
- Reused focused runner via --position-only and restored fresh build for changed sources. Regression/Guardian/UI smoke runs0; previous proofs retained.
- Saved evidence/state. Passive position values/state only; no Location provider/permission, background work, GPS/OsmAnd/Trip integration or persistence. P4 remains open; STOP.

## 2026-09-30 — P3 CLOSED / P4 READY / STOP
- Passed exactly one bounded API25/1024x600 regression81/81 and6/6 navigation returns. Reused12 unchanged exhaustive/concurrency/Diagnostics proofs with pinned source fingerprints;93 tests preserved.
- Reused verified Diagnostics APKs/Build/Lint after52 build-input and artifact hash checks; no source drift, rebuild or focused rerun. Added closure-only CI selection and pinned artifact retrieval. Fixed only a proven extraction-directory preparation issue before any regression ran.
- Production unchanged. Home/Apps/restart screenshots identical; truthful final P2/no-autoplay preserved, no observed crash/ANR. Updated complete evidence and state.
- Closed approved P3 passive scope. Automatic watchdog, actual recovery execution and persistent support export deferred to later integration/recovery. P4 is ready but was not started; STOP.

## 2026-09-29 — Guardian Diagnostics focused verification complete / STOP
- Build/Lint0 errors/17 unchanged warnings; GuardianDiagnosticRecordTest only2/2 PASS on API25/1024x600. Null/UNKNOWN and frozen healthy-session record assertions passed; no production defect/fix or test change needed.
- Added explicit --diagnostics-only mode to the existing emulator runner and selected it in CI. It exits before all regression/UI work. No full/bounded regression or old1024/6144 suite was run; earlier evidence retained.
- Saved focused evidence/source audit/artifact hashes and updated state. APK +608bytes, no observed crash/app ANR. This is a focused check only; STOP without another phase or actual Recovery.

## 2026-09-29 — Guardian Monitor Session consolidated verification closed / STOP
- Fixed one proven Session isolation defect: reset at Long.MAX_VALUE reused the expired generation token. Exhausted sessions now reject further heartbeats while keeping the counter saturated and cold health UNKNOWN. Preserved before/after focused proof.
- Added6 shared focused checks to4 supplied Session tests: all-component generation/order isolation, mixed diagnostic/result retention, null/empty behavior, explicit-only evaluation, saturation and200 bounded concurrent rounds. Existing81 tests and prior production primitives unchanged; no runtime dependency or worker added.
- Build/Lint0 errors/17 unchanged warnings; focused Java6/6 and API25 Session10/10, then ONE bounded regression74/74 (16 foundation +58 prior) and6/6 navigation returns PASS. Seven giant proofs reused with12 verified fingerprints; no1024/6144 regeneration or duplicate Session run.
- Home/Apps/restart PNGs identical; no autoplay/crash/ANR. APK +1,248bytes/PSS -131KB/launch +5ms are separate observations, not causal benchmarks. Updated evidence/state and closed the gate. STOP without Watchdog, Service or actual Recovery.

## 2026-09-29 — Guardian Monitoring Foundation consolidated verification closed / STOP
- Fixed proven new-bundle compile incompatibility in MonitorStep using a private Snapshot field adapter; corrected new test field references. Previous Guardian primitives remain unchanged.
- Fixed proven MonitorConfig Long.MAX_VALUE overflow, preserving a strictly later representable stale boundary. Retained before/after proof.
- Added10 shared focused checks to the supplied6 tests: ordering, liveness/config boundaries, journal semantics, full monitor composition/no-op/retention and two bounded200-round mutable-state concurrency checks. No runtime dependency or worker added.
- Focused Java10/10, API25 focused16/16, then exactly one regression74/74 +6/6 navigation returns PASS; Build/Lint0 errors/17 existing warnings.7 unchanged giant proofs reused with12 guarded source fingerprints per owner instruction; all65 prior tests remain in source.
- P2 screenshots identical; no autoplay/crash/ANR. APK +4,740bytes/PSS +185KB/launch +67ms are separate observations, not causal benchmarks. Updated evidence/state; STOP without automatic watchdog/service or actual recovery.

## 2026-09-29 — Guardian passive Supervisor verification closed / STOP
- Added8 tests for the full Snapshot -> Assessment -> Plan chain:6144 combination/level cases, null/cold/extreme inputs, nonmutation, repeated evaluation and retained results after update/reset. All57 prior tests preserved.
- Two test-only callers verified20000 composed evaluations during atomic health updates/reset, with exact internal state/revision/assessment/plan consistency and joined threads. No production worker added.
- Codeee62972 passed Build/Lint,65/65 API25 tests and6/6 navigation returns/Cold Restart/no-autoplay; CI captured2026-09-28. No application defect/fix needed.
- Home/Apps/restart images pixel-identical; APK +412bytes/PSS -48KB, no app crash/ANR; separate observations are not causal benchmarks.
- Saved evidence and updated state/gate. Caller-driven passive evaluation only; no watchdog, heartbeat, timers, logging or actual recovery.

## 2026-09-28 — Guardian recovery-policy verification closed / STOP
- Added7 tests covering6144 combination/level cases, conservative UNKNOWN/DEGRADED limits, exact FAILED ladder, negative/extreme levels, targets and immutable plan/input retention. All50 prior tests preserved.
- Code987884c passed Build/Lint,57/57 API25 tests and6/6 navigation returns/Cold Restart/no-autoplay. No application defect/fix needed.
- Home/Apps/restart images pixel-identical; APK +1,356bytes/PSS -155KB, no app crash/ANR; observations are not causal benchmarks.
- Saved evidence and updated state/gate. Recommendations only; no actual recovery, watchdog, logging or Supervisor.

## 2026-09-28 — Guardian assessment verification closed / STOP
- Added7 tests for all1024 classifications, exclusive membership/counts, null/cold/all-healthy, immutable retention after updates/reset and repeated frozen-input assessments. All43 prior tests retained.
- Codef1d53a6 passed Build/Lint,50/50 API25 tests and6/6 navigation returns/Cold Restart/no-autoplay. No application defect/fix.
- Home/Apps/restart images pixel-identical; APK +1,296bytes/PSS +92KB, no app crash/ANR; observations are not causal benchmarks.
- Updated evidence/results/state and closed gate. No watchdog/logging/recovery/background work.

## 2026-09-28 — Guardian snapshot verification closed / STOP
- Reproduced an inconsistent snapshot (CORE FAILED, overall HEALTHY) under concurrent updates; fixed capture to hold the existing registry monitor through component reads/aggregation. No production thread/background behavior added.
- Added7 snapshot tests covering null/cold, all1024 combinations, exact identities, retained values after updates/reset, repeated capture/recovery and20000 concurrent captures. All36 prior tests retained.
- Code3709e35 passed Build/Lint,43/43 API25 tests and6/6 navigation returns/Cold Restart/no-autoplay. Home/Apps/restart images pixel-identical.
- APK +340bytes/PSS +97KB, no app crash/ANR; resource observations are not causal benchmarks. Saved race proof and verification evidence, updated state and closed gate. No watchdog/logging.

## 2026-09-28 — Guardian aggregate-policy verification closed / STOP
- Added7 tests covering all1024 combinations, precedence, every sole problem source, null/cold boundaries, recovery/reset and unchanged snapshot identities/revisions. All29 prior tests retained.
- Codee42f2d6 passed Build/Lint,36/36 API25 tests and6/6 navigation returns/Cold Restart/no-autoplay. No application defect/fix.
- Home/Apps/restart images pixel-identical; APK +236bytes/PSS -26KB, no crash/ANR; observations are not causal benchmarks.
- Updated evidence/results/state and closed gate; no watchdog/logging/background work.

## 2026-09-28 — Guardian component-registry verification closed / STOP
- Added7 focused tests covering all5 components/60 distinct transitions, isolation, revisions, same/null identity, null component, retained state, repeated reset and FAILED-to-HEALTHY recovery. All22 prior tests retained.
- Code7ef7037 passed Build/Lint,29/29 API25 tests and6/6 navigation returns/Cold Restart/no-autoplay. No application defect/fix.
- Home/Apps/restart screenshots pixel-identical; APK +912bytes/PSS +79KB, no crash/ANR; observations are not causal benchmarks.
- Updated evidence/results/state and closed gate; no watchdog/logging/callbacks/background work.

## 2026-09-28 — Guardian health-state verification closed / STOP
- Added5 tests covering all4 states/all12 distinct transitions, revision increments, immutable retention, same/null identity and FAILED-to-HEALTHY recovery. Retained all17 prior tests.
- Codee994833 passed Build/Lint,22/22 API25 tests and6/6 navigation returns/Cold Restart/no-autoplay. No application defect/fix.
- Final P2 preserved; documented initial Home focus highlight and identical Apps/restart images. APK +536bytes/PSS -238KB, no crash/ANR; observations are not causal benchmarks.
- Updated evidence/results/state and closed gate; no watchdog/logging/background work.

## 2026-09-28 — Cold-reset publication verification closed / STOP
- Added3 focused reset/listener tests for exact-once synchronous delivery, snapshot identity, removal and repeatable cold states; all14 prior tests retained.
- Codecdc64b3 passed Build/Lint,17/17 API25 tests and6/6 quick-action returns/Cold Restart/no-autoplay. No application defect/fix needed.
- Home/Apps/restart images pixel-identical; APK -16 bytes/PSS -37KB; no observed crash/ANR. Resource observations are not causal benchmarks.
- Updated state/results/evidence and closed the gate. No Guardian/logging or subsequent work.

## 2026-09-28 — Core publish/subscription verification closed / STOP
- Added6 focused API25 tests for immutable revision/publish/listener identity, synchronous delivery, duplicate prevention, removal, null and cold-reset behavior; retained all8 prior tests.
- Code31ce9bc passed Build/Lint,14 tests and6 quick-action returns/Cold Restart/no-autoplay. No application defect/fix needed.
- UI images pixel-identical; recorded APK +712 bytes/PSS +173KB and single-launch observations with limitations, no crash/ANR.
- Updated evidence/state/results/gate; no Guardian/logging or subsequent P3 work.

## 2026-09-27 — P3 Core State verification closed / STOP
- Added3 focused API25 tests for truthful coldBoot, singleton snapshot/reset retention and fresh Activity versus recreation semantics; retained all5 UI tests.
- Code6bcc623 passed Build/Lint,8 tests,6 quick-action returns and Cold Restart/no-autoplay. No application defect/fix needed.
- Home/Apps/restart images pixel-identical to P2. Recorded APK +1,682 bytes, PSS +117KB and launch snapshots with benchmark limitations, no crash/ANR.
- Saved evidence and updated state/results/gate. No Guardian, logging or further P3 work.

## 2026-09-27 — Final-product UI verification closed / STOP
- Accepted current Darbak UI as final-product interface with truthful disconnected states; historical TEST-copy expectations superseded.
- Corrected enabled Apps actions without backends: disabled/dimmed them while preserving intended labels/RTL layout.
- Updated exact state tests and rejected temporary visible/accessibility wording; added Apps fit/RTL/unavailability test without dropping prior behavior checks.
- Code27e0e79 passed Build/Lint,5 API25 tests,6 quick-action returns and Cold Restart/no-autoplay; Home/Apps images reviewed.
- Saved evidence and updated state/results/gate; no P3/backend/next batch.

## 2026-09-27 — Stopped Media verification closed / STOP
- Corrected outdated idle-text smoke expectation for stopped TEST preview; application unchanged.
- Added exact media/navigation-test regressions and Cold Restart/settled Home checks with no app service/MediaSession assertions and raw diagnostics.
- Codeabc0b3d passed Build/Lint,4 API25 tests and6 quick-action returns; screenshots reviewed at1024x600 without clipping/overlap.
- Saved evidence and updated state/results/gate. No next batch or P3.

## 2026-09-27 — Navigation-card verification closed / STOP
- Verified existing b054d7d Build/Lint,4 API25 tests and6 quick-action round trips from run36325924001; no application change needed.
- Reviewed Home1024x600: no clipping/overlap; maneuver and ETA/distance each explicitly test-only, navigation unavailable visible.
- Rechecked all labels in eight captured Home states, retained evidence and updated status/results/gate. No next batch/P3.

## 2026-09-27 — Quiet Home verification closed / STOP
- Fixed proven clipping introduced by the new vehicle summary by merging the redundant title/summary.
- Labeled normal explicitly `(تجريبي)` beside the status, preserving global TEST and full stale warning.
- Extended existing Home assertions; code9ac0b10 passed Build/Lint,4 API25 tests and6 quick-action returns. Actual screenshot reviewed at1024x600.
- Saved failure/final evidence and updated state/results/gate. No next batch or P3.

## 2026-09-27 — Home quick-actions verification closed / STOP
- Added targeted emulator smoke checks for three Home quick actions, correct destination/selected tab, Android Back and Return Home, with label/56px target assertions.
- Code `cf062d6` passed Build/Lint,4 existing API25 tests and6 actual-tap round trips at1024x600.
- Reviewed final Home image; no clipping/overlap or app defect found. App code unchanged.
- Preserved evidence and updated state/gate/results. No additional P2 increment or P3 started.

## 2026-09-27 — P2 verification-only checkpoint / STOP
- Verified the existing Home unavailable/idle/stale labels from code `682d895`; main `17fe772` has identical application/build/test inputs.
- Reviewed successful Build/Lint, 4 API25/1024x600 tests, navigation/restart smoke and the actual Home screenshot.
- Confirmed no clipping/overlap and no stale vehicle value presented as live; no application fix was necessary.
- Preserved evidence and updated current status, gate closure and test results. No subsequent task or phase started.

## 2026-09-27 — P1 closed / initial emulator validation passed
- Final code `72fde4a` passed build/lint, four Android tests, UI navigation and cold restart on API25/x86 at 1024x600.
- Fixed RTL dashboard spacing after screenshot review and verified the exact 16dp gap.
- Recorded final APK hash/size, launch/PSS snapshots, CPU measurement limitation and clean crash/ANR result.
- Preserved selected evidence/screenshots, updated official status and defined only the next small P2 task.
- No T3/firmware/system work; no Stable declaration; no next batch started.

## 2026-09-27 — P1 implementation checkpoint (verification pending)
- Added separate Arabic RTL/landscape TEST shell with Home and placeholder navigation.
- Reused owner Launcher palette; reviewed Launcher.v2, TestStation and Dashline with license/compatibility notes.
- Added pinned Gradle/AGP, four Android instrumentation tests, emulator smoke/evidence workflow and build instructions.
- Local source checks passed; APK/runtime acceptance awaits remote CI because local Android tools/downloads are unavailable.

## P1 visual verification
- API25 build and tests passed; screenshot review found reversed relative spacing on the speed card. Corrected RTL margin and added a measured 16dp gap assertion to the existing fit test.

## P1 CI environment fix
- First remote run exposed missing sdkmanager on Ubuntu24.04; explicitly provision Android SDK tools. Still within the original P1 verification batch.

## Planning baseline v1.0
- Established Darbak OS dedicated project repository.
- Added Work master plan and continuation/checkpoint workflow.
- Locked target hardware/API constraints.
- Locked firmware/MCU/Golden Backup safety rules.
- Added laptop-first testing path.
- Added reuse-first reference policy.


## 2026-10-02 — P7 software foundation closed
- Added truthful installed-app discovery and Android app-management entry without adding another launcher framework.
- Added dedicated persisted user Settings while keeping technical Admin separate.
- Added lightweight child-safe Standby that preserves TripRuntime continuity.
- Added quiet-by-default actionable alerts; missing GPS permission is actionable while ordinary unavailable vehicle hardware stays non-alarming.
- Final focused API25 run `37011339702` passed P5/P6 plus all P7 gates. No Full Regression or Guardian suites.
- Advanced the project to P8 Update/Admin/Recovery foundation; real recovery/Golden Backup remains hardware-gated to P9.


## 2026-10-02 — P8 software foundation closed
- Added hidden read-only technical Admin diagnostics.
- Added read-only local Darbak APK inspection with SHA-256 and package/version identity; no installer execution.
- Added explicit Recovery Readiness contract requiring Golden Backup identity, valid SHA-256 and verified recovery path.
- Final focused API25 run `37014555613` passed preserved P5-P7 and all P8 gates. No Full Regression or Guardian suites.
- Advanced to P9 exact T3 baseline + Golden Backup/recovery verification gate.