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
    time.sleep(1.5)


def dump_nodes():
    remote = "/sdcard/darbak-p10-permission.xml"
    adb("shell", "uiautomator", "dump", remote)
    with tempfile.TemporaryDirectory() as td:
        local = Path(td) / "window.xml"
        adb("pull", remote, str(local))
        root = ET.parse(local).getroot()
        return list(root.iter("node"))


def node_text(node):
    return (node.get("text") or "").strip().lower()


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
    time.sleep(0.5)


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
    )
    for suffix in preferred_ids:
        for node in nodes:
            if (node.get("resource-id") or "").endswith(suffix):
                return node
    for node in nodes:
        text = node_text(node)
        if "while using" in text or "only this time" in text:
            return node
    return None


def assert_real_location_dialog(nodes):
    serialized = "\n".join(
        (node.get("package") or "") + " " + (node.get("resource-id") or "") + " " + node_text(node)
        for node in nodes
    ).lower()
    assert "permissioncontroller" in serialized, "system permission controller dialog not visible"
    assert "precise" in serialized, "Precise location choice not visible"
    assert "approximate" in serialized, "Approximate location choice not visible"


def choose_location(precise):
    nodes = dump_nodes()
    assert_real_location_dialog(nodes)
    choice = find_choice(nodes, "precise" if precise else "approximate")
    assert choice is not None
    tap_node(choice)
    nodes = dump_nodes()
    allow = find_allow(nodes)
    assert allow is not None, "foreground location allow button not found"
    tap_node(allow)
    time.sleep(1.5)


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
