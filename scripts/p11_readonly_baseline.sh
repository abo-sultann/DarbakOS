#!/usr/bin/env bash
set -u

# GDN P11 — read-only production head-unit baseline collector.
# Safety: this script only reads device state through adb. It does not install,
# grant, write settings, reboot, root, remount, push, pull, delete or flash.

OUT_ROOT="${1:-p11-evidence}"
STAMP="$(date +%Y%m%d-%H%M%S)"
OUT_DIR="$OUT_ROOT/baseline-$STAMP"
mkdir -p "$OUT_DIR"

if ! command -v adb >/dev/null 2>&1; then
  echo "ERROR: adb not found in PATH" >&2
  exit 2
fi

ADB_STATE="$(adb get-state 2>/dev/null || true)"
if [ "$ADB_STATE" != "device" ]; then
  echo "ERROR: no authorized adb device is ready (state: ${ADB_STATE:-unknown})" >&2
  adb devices -l || true
  exit 3
fi

run() {
  local name="$1"
  shift
  {
    echo "# command: $*"
    echo "# captured_utc: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
    echo
    "$@"
  } >"$OUT_DIR/$name.txt" 2>&1 || true
}

shell() {
  local name="$1"
  shift
  run "$name" adb shell "$@"
}

run host_adb adb version
run adb_device adb devices -l

shell getprop getprop
shell android_version getprop ro.build.version.release
shell api_level getprop ro.build.version.sdk
shell security_patch getprop ro.build.version.security_patch
shell build_fingerprint getprop ro.build.fingerprint
shell manufacturer getprop ro.product.manufacturer
shell model getprop ro.product.model
shell device_name getprop ro.product.device
shell hardware getprop ro.hardware
shell board_platform getprop ro.board.platform
shell abi_list getprop ro.product.cpu.abilist
shell cpuinfo cat /proc/cpuinfo
shell meminfo cat /proc/meminfo
shell storage df -h
shell mounts cat /proc/mounts
shell display_size wm size
shell display_density wm density
shell display_dumpsys dumpsys display
shell graphics_dumpsys dumpsys SurfaceFlinger
shell usb_state dumpsys usb
shell usb_props sh -c 'getprop | grep -i usb'
shell location_dumpsys dumpsys location
shell audio_dumpsys dumpsys audio
shell media_session_dumpsys dumpsys media_session
shell power_dumpsys dumpsys power
shell activity_processes dumpsys activity processes
shell package_gdn sh -c 'pm list packages | grep -Ei "gdn|darbak|osmand"'
shell packages_all pm list packages
shell launcher_candidates sh -c 'cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.LAUNCHER 2>/dev/null || true'
shell settings_display sh -c 'settings get system screen_brightness; settings get system screen_brightness_mode; settings get system font_scale'
shell settings_location sh -c 'settings get secure location_mode; settings get secure location_providers_allowed'
shell uptime uptime
shell thermal sh -c 'cat /sys/class/thermal/thermal_zone*/temp 2>/dev/null || true'

cat >"$OUT_DIR/README.txt" <<EOF
GDN P11 read-only baseline capture
Captured UTC: $(date -u +%Y-%m-%dT%H:%M:%SZ)
ADB state: $ADB_STATE

Safety contract:
- read-only evidence collection only
- no adb root/remount
- no settings writes
- no install/uninstall
- no reboot
- no firmware/MCU/kernel changes
- no file deletion or device mutation

Next step: attach this directory to the P11 commissioning evidence and review before any integration work.
EOF

printf 'P11 baseline captured: %s\n' "$OUT_DIR"
