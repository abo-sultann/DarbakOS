#!/usr/bin/env python3
"""P10 Android 17 audio-permission smoke using the real system dialog."""
from pathlib import Path
import re
import subprocess
import tempfile
import time
import xml.etree.ElementTree as ET

PKG = "com.abosultan.darbakos.test"
ACTIVITY = PKG + "/com.abosultan.darbakos.MainActivity"
APK = "app/build/outputs/apk/debug/app-debug.apk"
AUDIO = "android.permission.READ_MEDIA_AUDIO"


def adb(*args, check=True):
    p = subprocess.run(["adb", *args], text=True, capture_output=True)
    if check and p.returncode:
        raise RuntimeError((p.stdout + p.stderr).strip())
    return (p.stdout + p.stderr).strip()


def permission_granted(permission):
    out = adb("shell", "dumpsys", "package", PKG)
    m = re.search(r"^\s*" + re.escape(permission) + r": granted=(true|false)", out, re.M)
    return bool(m and m.group(1) == "true")


def dump_nodes():
    remote = "/sdcard/darbak-p10-ui.xml"
    adb("shell", "uiautomator", "dump", remote)
    with tempfile.TemporaryDirectory() as td:
        local = Path(td) / "window.xml"
        adb("pull", remote, str(local))
        return list(ET.parse(local).getroot().iter("node"))


def center(node):
    m = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.get("bounds") or "")
    if not m:
        raise RuntimeError("missing node bounds")
    x1, y1, x2, y2 = map(int, m.groups())
    return (x1 + x2) // 2, (y1 + y2) // 2


def tap(node):
    x, y = center(node)
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(0.7)


def by_id(nodes, suffixes):
    if isinstance(suffixes, str):
        suffixes = (suffixes,)
    for suffix in suffixes:
        for node in nodes:
            if (node.get("resource-id") or "").endswith(suffix):
                return node
    return None


adb("uninstall", PKG, check=False)
adb("install", APK)
assert not permission_granted(AUDIO), "READ_MEDIA_AUDIO unexpectedly pre-granted"
adb("shell", "am", "start", "-W", "-n", ACTIVITY)
time.sleep(1.2)

# Darbak asks for location on first foreground start. Deny it through the system dialog so the
# media flow can be exercised independently; do not use adb to grant any runtime permission.
nodes = dump_nodes()
deny = by_id(nodes, ("permission_deny_button", "permission_deny_and_dont_ask_again_button"))
assert deny is not None, "initial location permission dialog not found"
tap(deny)

nodes = dump_nodes()
media_nav = by_id(nodes, "nav_media")
assert media_nav is not None, "Media navigation button not found"
tap(media_nav)

nodes = dump_nodes()
scan = by_id(nodes, "media_local_scan_button")
assert scan is not None, "Local audio scan button not found"
tap(scan)
time.sleep(0.8)

nodes = dump_nodes()
serialized = "\n".join(
    (n.get("package") or "") + " " + (n.get("resource-id") or "") + " " + (n.get("text") or "")
    for n in nodes
).lower()
assert "permissioncontroller" in serialized, "system audio permission dialog not visible"
allow = by_id(nodes, ("permission_allow_button", "permission_allow_foreground_only_button"))
if allow is None:
    for node in nodes:
        text = (node.get("text") or "").strip().lower()
        if text == "allow" or text.startswith("allow "):
            allow = node
            break
assert allow is not None, "audio permission Allow button not found"
tap(allow)
time.sleep(0.8)
assert permission_granted(AUDIO), "READ_MEDIA_AUDIO was not granted by the real permission dialog"

print("PASS: Android 17 READ_MEDIA_AUDIO is requested and granted through the real system dialog")
