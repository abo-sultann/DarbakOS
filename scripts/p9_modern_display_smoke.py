#!/usr/bin/env python3
"""P9 modern-display smoke. Uses an already booted emulator/device; no privileged actions."""
import subprocess, time, sys

def adb(*args, check=True):
    p=subprocess.run(["adb",*args],text=True,capture_output=True)
    if check and p.returncode: raise RuntimeError(p.stdout+p.stderr)
    return (p.stdout+p.stderr).strip()

apk="app/build/outputs/apk/debug/app-debug.apk"
adb("install","-r",apk)
adb("shell","am","force-stop","com.abosultan.darbakos.test")
adb("shell","monkey","-p","com.abosultan.darbakos.test","-c","android.intent.category.LAUNCHER","1")
time.sleep(2)
out=adb("shell","dumpsys","window","windows")
if "com.abosultan.darbakos.test" not in out:
    raise SystemExit("FAIL: GDN is not foreground")
size=adb("shell","wm","size")
density=adb("shell","wm","density")
errors=adb("logcat","-d","-t","300")
fatal=[x for x in errors.splitlines() if ("FATAL EXCEPTION" in x or "ANR in com.abosultan.darbakos.test" in x)]
if fatal:
    print("\n".join(fatal)); raise SystemExit("FAIL: crash/ANR detected")
print("PASS: modern display launch smoke")
print(size); print(density)
