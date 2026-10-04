#!/usr/bin/env python3
"""Offline source guard for current Darbak OS constraints; not a substitute for Android tests."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
A = '{http://schemas.android.com/apk/res/android}'
main = ROOT / 'app/src/main'
for path in main.rglob('*.xml'):
    ET.parse(path)
manifest = ET.parse(main / 'AndroidManifest.xml').getroot()
app = manifest.find('application')
assert app.get(A + 'supportsRtl') == 'true'
permissions = {item.get(A + 'name') for item in manifest.findall('uses-permission')}
assert permissions == {
    'android.permission.ACCESS_FINE_LOCATION',
    'android.permission.READ_EXTERNAL_STORAGE',
    'android.permission.FOREGROUND_SERVICE',
    'android.permission.FOREGROUND_SERVICE_LOCATION',
}, 'Only approved location/media permissions are allowed at this checkpoint'

services = {item.get(A + 'name'): item for item in app.findall('service')}
assert set(services) == {'.core.TripRuntimeService', '.core.DarbakMediaNotificationListener'}, \
    'Only the approved Trip runtime and system-bound media access services are allowed'
trip = services['.core.TripRuntimeService']
assert trip.get(A + 'exported') == 'false' and not trip.findall('intent-filter')
assert trip.get(A + 'process') is None
assert trip.get(A + 'foregroundServiceType') == 'location', \
    'Modern Trip runtime must explicitly declare the location foreground-service type'
media = services['.core.DarbakMediaNotificationListener']
assert media.get(A + 'exported') == 'true'
assert media.get(A + 'permission') == 'android.permission.BIND_NOTIFICATION_LISTENER_SERVICE'
filters = media.findall('intent-filter')
assert len(filters) == 1
assert {a.get(A + 'name') for a in filters[0].findall('action')} == {
    'android.service.notification.NotificationListenerService'
}
assert media.get(A + 'process') is None and not app.findall('receiver'), \
    'No extra process or broadcast receiver is approved'

activity = app.find('activity')
assert activity.get(A + 'screenOrientation') == 'landscape'
assert all(c.get(A + 'name') != 'android.intent.category.HOME'
           for c in activity.findall('.//category')), 'Darbak must not take over the device launcher yet'
build = (ROOT / 'app/build.gradle').read_text()
assert re.search(r'minSdk\s+25\b', build)
assert not re.search(r'^\s*(?:implementation|api|runtimeOnly)\b', build, re.M)
assert not list(main.rglob('*.so')), 'No native ABI dependency expected'
strings = ET.parse(main / 'res/values/strings.xml').getroot()
assert all(not re.search(r'TEST|experimental|preview|prototype|تجريب|معاينة|اختبار', item.text or '', re.I)
           for item in strings), 'No temporary user-facing copy'
java = '\n'.join(p.read_text() for p in main.rglob('*.java'))
assert 'androidx.media' not in java, 'P5 must remain on lightweight platform media APIs'
assert 'BluetoothAdapter' not in java
assert 'LocationManager' in java, 'P4 GPS source must remain explicit and reviewable'
assert 'MediaSessionManager' in java and 'MediaController' in java, \
    'P5 media integration must remain explicit and platform-based'
runtime = main / 'java/com/abosultan/darbakos/core/TripRuntimeService.java'
media_library = main / 'java/com/abosultan/darbakos/core/LocalMediaLibrary.java'
runtime_text = runtime.read_text()
assert runtime_text.count('new HandlerThread(') == 1
assert runtime_text.count('new Handler(') == 1
assert 'startForeground(' in runtime_text and 'NotificationChannel' in runtime_text, \
    'Modern Trip runtime must enter foreground mode on API26+'
assert media_library.read_text().count('new HandlerThread(') == 1
assert media_library.read_text().count('new Handler(') == 1
for path in main.rglob('*.java'):
    source = path.read_text()
    assert not re.search(r'new\s+(?:Thread|Timer)\s*\(|ExecutorService|Executors\.', source), path
    if path not in (runtime, media_library):
        assert not re.search(r'new\s+(?:HandlerThread|Handler)\s*\(', source), path
print('PASS: API25 legacy floor + modern location FGS contract, RTL, P4/P5 boundaries, no extra process/receiver/runtime dependency/native code')
