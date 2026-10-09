#!/usr/bin/env python3
"""P10 foreground/background integration smoke with the CI OsmAnd fixture."""
from pathlib import Path
import re
import subprocess
import tempfile
import time
import xml.etree.ElementTree as ET

PKG = "com.abosultan.darbakos.test"
ACTIVITY = PKG + "/com.abosultan.darbakos.MainActivity"
OSMAND = "net.osmand"


def adb(*args, check=True):
    p = subprocess.run(["adb", *args], text=True, capture_output=True)
    if check and p.returncode:
        raise RuntimeError((p.stdout + p.stderr).strip())
    return (p.stdout + p.stderr).strip()


def trip_running():
    return "TripRuntimeService" in adb("shell", "dumpsys", "activity", "services", PKG)


def current_window():
    return adb("shell", "dumpsys", "window", "windows")


def dump_nodes():
    remote = "/sdcard/darbak-p10-osmand.xml"
    adb("shell", "uiautomator", "dump", remote)
    with tempfile.TemporaryDirectory() as td:
        local = Path(td) / "window.xml"
        adb("pull", remote, str(local))
        return list(ET.parse(local).getroot().iter("node"))


def tap_id(suffix):
    for node in dump_nodes():
        if (node.get("resource-id") or "").endswith(suffix):
            bounds = node.get("bounds") or ""
            m = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
            if not m:
                break
            x1, y1, x2, y2 = map(int, m.groups())
            adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
            time.sleep(0.7)
            return
    raise RuntimeError("UI node not found: " + suffix)


adb("shell", "am", "force-stop", PKG)
adb("shell", "am", "start", "-W", "-n", ACTIVITY)
time.sleep(1.0)
assert PKG in current_window(), "GDN did not reach foreground"
assert trip_running(), "Trip runtime is not running before opening OsmAnd"

tap_id("nav_map")
tap_id("map_open_button")
time.sleep(1.0)
assert OSMAND in current_window(), "OsmAnd fixture did not become foreground"
assert trip_running(), "Trip runtime stopped when OsmAnd became foreground"

adb("shell", "input", "keyevent", "4")
time.sleep(1.0)
assert PKG in current_window(), "GDN did not return after Back from OsmAnd"
assert trip_running(), "Trip runtime did not survive OsmAnd round-trip"

print("PASS: TripRuntimeService remains active while OsmAnd is foreground and after returning")
