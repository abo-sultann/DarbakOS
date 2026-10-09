#!/usr/bin/env python3
"""Bounded API25 verification for GDN P6 Vehicle Data foundation."""
from pathlib import Path
import json
import subprocess

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'test-evidence'
OUT.mkdir(exist_ok=True)
PACKAGE = 'com.abosultan.darbakos.test'
serials = [line.split()[0] for line in subprocess.check_output(['adb','devices'], text=True).splitlines()[1:]
           if line.endswith('\tdevice') and line.startswith('emulator-')]
assert len(serials) == 1
ADB = ['adb','-s',serials[0]]

def adb(*args, binary=False):
    return subprocess.check_output(ADB + list(args), text=not binary, timeout=60)

def save(name, value):
    path=OUT/name
    path.write_bytes(value) if isinstance(value,bytes) else path.write_text(value)

assert adb('shell','getprop','ro.build.version.sdk').strip() == '25'
selected='com.abosultan.darbakos.VehicleDataTest'
output=subprocess.check_output(ADB+['shell','am','instrument','-w','-e','class',selected,
    PACKAGE+'.test/androidx.test.runner.AndroidJUnitRunner'], text=True, timeout=180)
save('p6-vehicle-instrumentation.txt',output)
assert 'OK (4 tests)' in output and 'FAILURES' not in output, output
crash=adb('logcat','-b','crash','-d')
save('p6-final-crash.txt',crash)
assert 'FATAL EXCEPTION' not in crash, crash
save('p6-summary.json',json.dumps({
    'result':'PASS',
    'commit':subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip(),
    'api':25,
    'focused_tests':4,
    'unavailable':True,
    'fresh':True,
    'stale':True,
    'source_attribution':True,
    'full_regression_runs':0,
    'guardian_suite_runs':0,
    'physical_vehicle_sources_required':False
},indent=2))
print('PASS: P6 Vehicle Data foundation; 4 focused tests')
