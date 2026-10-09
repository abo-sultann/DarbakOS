#!/usr/bin/env python3
"""P10 Android 17 audio-permission smoke using the real system dialog.

Location behavior has its own clean-install test. This test keeps unrelated location access denied
and fixed so the first foreground launch cannot obscure the READ_MEDIA_AUDIO flow.
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
AUDIO = "android.permission.READ_MEDIA_AUDIO"
COARSE = "android.permission.ACCESS_COARSE_LOCATION"
FINE = "android.permission.ACCESS_FINE_LOCATION"


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
    remote = "/sdcard/darbak-p10-media-ui.xml"
    adb("shell", "uiautomator", "dump", remote)
    with tempfile.TemporaryDirectory() as td:
        local = Path(td) / "window.xml"
        adb("pull", remote, str(local))
        return list(ET.parse(local).getroot().iter("node"))


def text(node):
    return ((node.get("text") or "") + " " + (node.get("content-desc") or "")).strip().lower()


def center(node):
    m = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.get("bounds") or "")
    if not m:
        raise RuntimeError("missing node bounds")
    x1, y1, x2, y2 = map(int, m.groups())
    return (x1 + x2) // 2, (y1 + y2) // 2


def tap(node):
    x, y = center(node)
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(0.6)


def by_id(nodes, suffixes):
    if isinstance(suffixes, str):
        suffixes = (suffixes,)
    for suffix in suffixes:
        for node in nodes:
            if (node.get("resource-id") or "").endswith(suffix):
                return node
    return None


def dismiss_transient_system_overlay(nodes):
    for node in nodes:
        package = (node.get("package") or "").lower()
        rid = (node.get("resource-id") or "").lower()
        if package == "com.android.systemui" and (rid.endswith(":id/ok") or rid.endswith("/ok")):
            tap(node)
            return True
    return False


def wait_for_node(resource_suffix, timeout=12.0):
    deadline = time.monotonic() + timeout
    last = []
    while time.monotonic() < deadline:
        last = dump_nodes()
        if dismiss_transient_system_overlay(last):
            continue
        node = by_id(last, resource_suffix)
        if node is not None:
            return node
        time.sleep(0.35)
    rendered = "\n".join(
        " | ".join((n.get("package") or "", n.get("resource-id") or "", text(n)))
        for n in last
    )
    raise AssertionError(resource_suffix + " not found\n" + rendered[-8000:])


def wait_for_audio_dialog(timeout=12.0):
    deadline = time.monotonic() + timeout
    last = []
    while time.monotonic() < deadline:
        last = dump_nodes()
        if dismiss_transient_system_overlay(last):
            continue
        serialized = "\n".join(
            (n.get("package") or "") + " " + (n.get("resource-id") or "") + " " + text(n)
            for n in last
        ).lower()
        if "permissioncontroller" in serialized and ("music" in serialized or "audio" in serialized):
            return last
        time.sleep(0.35)
    raise AssertionError("system audio permission dialog not visible")


def find_allow(nodes):
    node = by_id(nodes, (
        "permission_allow_button",
        "permission_allow_foreground_only_button",
    ))
    if node is not None:
        return node
    for candidate in nodes:
        value = text(candidate)
        if value == "allow" or value.startswith("allow "):
            return candidate
    return None


def find_deny(nodes):
    node = by_id(nodes, (
        "permission_deny_button",
        "permission_deny_and_dont_ask_again_button",
    ))
    if node is not None:
        return node
    for candidate in nodes:
        value = text(candidate)
        if "don’t allow" in value or "don't allow" in value or value == "deny":
            return candidate
    return None


adb("uninstall", PKG, check=False)
adb("install", APK)
assert not permission_granted(AUDIO), "READ_MEDIA_AUDIO unexpectedly pre-granted"

# Keep location denied for this focused media test without granting it through adb.
for permission in (COARSE, FINE):
    adb("shell", "pm", "revoke", PKG, permission, check=False)
    adb("shell", "pm", "set-permission-flags", PKG, permission, "user-set", "user-fixed", check=False)

adb("shell", "am", "start", "-W", "-n", ACTIVITY)
media_nav = wait_for_node("nav_media")
tap(media_nav)
scan = wait_for_node("media_local_scan_button")
tap(scan)

# First request: deny through the real Android 17 permission controller.
nodes = wait_for_audio_dialog()
deny = find_deny(nodes)
assert deny is not None, "audio permission Deny button not found"
tap(deny)
time.sleep(0.6)
assert not permission_granted(AUDIO), "READ_MEDIA_AUDIO unexpectedly granted after denial"

# Second request: ask again and allow through the real permission controller.
scan = wait_for_node("media_local_scan_button")
tap(scan)
nodes = wait_for_audio_dialog()
allow = find_allow(nodes)
assert allow is not None, "audio permission Allow button not found"
tap(allow)
time.sleep(0.8)
assert permission_granted(AUDIO), "READ_MEDIA_AUDIO was not granted by the real permission dialog"

print("PASS: Android 17 READ_MEDIA_AUDIO clean-install dialog covers deny then allow while location stays denied")
