#!/usr/bin/env python3
"""P10 clean-install permission smoke for Android 17.

Exercises the real system permission dialog. No adb pm grant is used for location.
The first clean install chooses Approximate and verifies the trip runtime stays stopped.
The second clean install chooses Precise and verifies the foreground trip runtime starts.
Finally, fine location is revoked to verify the next foreground launch does not restart the runtime.
"""
from pathlib import Path
import re
import subprocess
import tempfile
import time
import xml.etree.ElementTree as ET

PKG = "com.abosultan.darbakos.test"
ACTIVITY = PKG + "/com.abosultan.darbakos.MainActivity"
APK = "app/build/outputs/apk/debug/app-debug.apk"
COARSE = "android.permission.ACCESS_COARSE_LOCATION"
FINE = "android.permission.ACCESS_FINE_LOCATION"


def adb(*args, check=True):
    p = subprocess.run(["adb", *args], text=True, capture_output=True)
    if check and p.returncode:
        raise RuntimeError((p.stdout + p.stderr).strip())
    return (p.stdout + p.stderr).strip()


def permission_granted(permission):
    out = adb("shell", "dumpsys", "package", PKG)
    pattern = re.compile(r"^\s*" + re.escape(permission) + r": granted=(true|false)", re.M)
    match = pattern.search(out)
    return bool(match and match.group(1) == "true")


def clean_install():
    adb("uninstall", PKG, check=False)
    adb("install", APK)
    assert not permission_granted(COARSE), "coarse location unexpectedly pre-granted"
    assert not permission_granted(FINE), "fine location unexpectedly pre-granted"
    adb("shell", "am", "start", "-W", "-n", ACTIVITY)


def dump_nodes():
    remote = "/sdcard/darbak-p10-permission.xml"
    adb("shell", "uiautomator", "dump", remote)
    with tempfile.TemporaryDirectory() as td:
        local = Path(td) / "window.xml"
        adb("pull", remote, str(local))
        root = ET.parse(local).getroot()
        return list(root.iter("node"))


def node_text(node):
    text = (node.get("text") or "") + " " + (node.get("content-desc") or "")
    return text.strip().lower()


def serialize(nodes):
    return "\n".join(
        " | ".join((
            node.get("package") or "",
            node.get("resource-id") or "",
            node.get("class") or "",
            node_text(node),
        ))
        for node in nodes
    )


def bounds_center(node):
    bounds = node.get("bounds") or ""
    match = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
    if not match:
        raise RuntimeError("node has no tappable bounds: " + bounds)
    x1, y1, x2, y2 = map(int, match.groups())
    return (x1 + x2) // 2, (y1 + y2) // 2


def tap_node(node):
    x, y = bounds_center(node)
    adb("shell", "input", "tap", str(x), str(y))


def find_choice(nodes, word):
    word = word.lower()
    for node in nodes:
        if word in node_text(node):
            return node
    return None


def find_allow(nodes):
    preferred_ids = (
        "permission_allow_foreground_only_button",
        "permission_allow_one_time_button",
        "permission_allow_button",
    )
    for suffix in preferred_ids:
        for node in nodes:
            if (node.get("resource-id") or "").endswith(suffix):
                return node
    for node in nodes:
        text = node_text(node)
        if "while using" in text or "only this time" in text or text == "allow":
            return node
    return None


def is_system_permission_surface(nodes):
    for node in nodes:
        package = (node.get("package") or "").lower()
        resource_id = (node.get("resource-id") or "").lower()
        if "permissioncontroller" in package:
            return True
        if package and package != PKG and "permission_" in resource_id:
            return True
    return False


def wait_for_location_dialog(timeout=12.0):
    deadline = time.monotonic() + timeout
    last = []
    while time.monotonic() < deadline:
        try:
            last = dump_nodes()
        except RuntimeError:
            time.sleep(0.4)
            continue
        if (is_system_permission_surface(last)
                and find_choice(last, "precise") is not None
                and find_choice(last, "approximate") is not None):
            return last
        time.sleep(0.4)
    focused = adb("shell", "dumpsys", "window", "windows", check=False)
    raise AssertionError(
        "location permission dialog did not become ready within %.1fs\n"
        "--- focused windows ---\n%s\n--- last UI tree ---\n%s"
        % (timeout, focused[-4000:], serialize(last)[-8000:])
    )


def wait_for_allow(timeout=8.0):
    deadline = time.monotonic() + timeout
    last = []
    while time.monotonic() < deadline:
        last = dump_nodes()
        allow = find_allow(last)
        if is_system_permission_surface(last) and allow is not None:
            return allow
        time.sleep(0.3)
    raise AssertionError("foreground location Allow button not found\n" + serialize(last)[-8000:])


def choose_location(precise):
    nodes = wait_for_location_dialog()
    choice = find_choice(nodes, "precise" if precise else "approximate")
    assert choice is not None
    tap_node(choice)
    allow = wait_for_allow()
    tap_node(allow)
    time.sleep(1.2)


def trip_runtime_running():
    out = adb("shell", "dumpsys", "activity", "services", PKG)
    return "TripRuntimeService" in out


# Clean install #1: user deliberately grants Approximate only.
clean_install()
choose_location(False)
assert permission_granted(COARSE), "Approximate choice did not grant coarse location"
assert not permission_granted(FINE), "Approximate choice unexpectedly granted fine location"
assert not trip_runtime_running(), "Trip runtime must not start with approximate-only location"

# Clean install #2: user grants Precise through the real permission dialog.
clean_install()
choose_location(True)
assert permission_granted(COARSE), "Precise choice must include coarse location"
assert permission_granted(FINE), "Precise choice did not grant fine location"
assert trip_runtime_running(), "Trip runtime did not start after precise permission was granted"

# Withdrawal path: simulate the user removing precise access after it was granted.
adb("shell", "pm", "revoke", PKG, FINE)
time.sleep(1.0)
assert not permission_granted(FINE), "fine location revocation did not take effect"
adb("shell", "am", "start", "-W", "-n", ACTIVITY)
time.sleep(1.0)
assert not trip_runtime_running(), "Trip runtime must remain stopped after fine location is withdrawn"

print("PASS: actual permission dialog covers approximate/precise and withdrawn fine location prevents runtime restart")
