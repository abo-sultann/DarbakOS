# غضن | GDN — Identity audit

**Date:** 2026-10-10 (Asia/Riyadh)

**Branch:** `p10-review-fixes-20261006`

**Inspected head:** `7d64c660d6998de00c1fcf9f68b385a706df623a`

Searched tracked text and filenames at this head for `Darbak`, `DarbakOS` and `دربك`, including lowercase identifiers. This is a bounded identity audit, not a migration or P10 release approval. The three categories below cover the findings; uncertain identifiers are retained under B.

## A) Changed now

- Trip foreground-notification title and channel display name now use the existing `brand` resource, **غضن | GDN**. The channel ID, notification ID, worker and service behavior are unchanged.
- Current Master Plan title/product descriptions and the undated current section of `REFERENCES.md` use GDN. The Master Plan filename is unchanged.
- Media fixture application label, screen text, track titles and artist metadata use GDN; the current integration-test expectations were updated together. Test-generated audio titles/artists also use GDN.
- Safe comments in the Manifest, media listener, OsmAnd bridge/package helper, trip reader, trip service, vehicle adapter and current test helpers use GDN.
- Current smoke-script descriptions and failure messages use GDN. A method-local parameter is now `uiVisible`; an automatically deleted monitoring temporary-directory prefix is now `gdn-monitoring-`.

These edits remove 56 legacy-name occurrences from the inspected head, across 25 existing files. No package, class, component or repository filename was renamed.

## B) Retained for compatibility / conservative deferral

| Retained item | Reason |
|---|---|
| `com.abosultan.darbakos`, test/fixture application IDs, imports and source-directory paths | Installed-app, build, instrumentation and component identity; requires coordinated migration. |
| `darbak_local_media` and all saved-data keys | Existing local-media data must remain readable. |
| `darbak_trip_runtime` | Persisted notification-channel identity and user settings; only its display name changed. |
| `DarbakMediaNotificationListener` and its Manifest/test references | Android notification-listener grants bind to this component. |
| `DarbakState` and its consumers | Shared internal contract; class migration is outside this text-only audit. |
| `DarbakOS-update.apk` | Existing update-package filename contract. |
| `ic_darbak`, `Theme.Darbak` | Manifest/resource references; modern UI resources are explicitly out of scope. |
| `rootProject.name = 'DarbakOS'`, repository URLs and `00_Darbak_OS_Master_Plan_Work_v1.0.md` | Preserve build/tooling identity, real repository links and existing document links. |
| CI command-line-tool/AVD/PID names, `DarbakOS-compatibility-*`, `/sdcard/darbak-*.xml`, test cache/audio paths | Conservative retention of automation, evidence and file-path conventions. No external consumer is assumed safe to rename. |
| `DarbakTripRuntime`, `DarbakLocalMedia`, `DarbakRuntimeQA`, `DarbakP5Fixture` | Diagnostic/thread/session conventions; some are asserted directly by existing runners/tests. |
| Old-label fallback in `scripts/p5_media_smoke.py` | Continues recognizing legacy notification-access labels alongside GDN. |
| Comments in `CoreStateStore` / `GuardianState` | Preserve source fingerprints referenced by retained proof manifests; no proof regeneration for a comment-only rename. |
| Comments in `MainActivity`, `RuntimeStartPolicy`, `StartupCoordinator` | Conservative deferral under the explicit modern-UI/startup scope restrictions. |
| Quoted legacy names in README, current status, next-task and P10 review notes | They explain retained compatibility identifiers or the audit itself, not current branding. |

The remaining occurrences in this category are technical identifiers, their references, or explicitly deferred comments. This audit does not claim that every deferred internal name inherently requires data migration.

## C) Historical; unchanged

- `TEST_RESULTS.md` and all `docs/test-evidence/` contents, including captured UI, logs, package paths, artifacts, filenames, hashes and historical test source.
- Dated P1/P3/P4 reuse decisions in `REFERENCES.md`, including original upstream repository names/URLs.
- Historical naming explanation in `CHANGELOG.md`.
- `docs/P1_BUILD_AND_TEST.md`, its old P1/P3 runner `scripts/emulator_smoke.py` and its historical `دربك OS` badge assertion. The current workflow uses the focused API25/P10 runners; this audit does not reactivate or modernize that old runner.
- Retired-device document `docs/T3_GOLDEN_BACKUP_CHECKLIST.md` and original proof manifests `scripts/guardian_reused_proofs.json` / `scripts/p3_closure_reuse.json`.

Validation: existing `check_baseline.py` and `p9_gate_check.py` passed; changed Python/XML parsed; `git diff --check` passed. Package/import identity, saved preferences/channel identifiers, Manifest component contract, API build settings, modern UI/startup files and historical sections were checked against the inspected head. Fixture display text and integration expectations agree. Android build/emulator execution is not claimed by these local checks (no local Android SDK).

`minSdk 25`, `compileSdk 37` and `targetSdk 37` are unchanged. No Firmware/MCU/Root/Boot work, UI redesign, bulk rename, migration, PR merge or follow-on project task is part of this audit.
