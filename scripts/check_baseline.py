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

permission_nodes = {item.get(A + 'name'): item for item in manifest.findall('uses-permission')}
assert set(permission_nodes) == {
    'android.permission.ACCESS_COARSE_LOCATION',
    'android.permission.ACCESS_FINE_LOCATION',
    'android.permission.READ_EXTERNAL_STORAGE',
    'android.permission.READ_MEDIA_AUDIO',
    'android.permission.FOREGROUND_SERVICE',
    'android.permission.FOREGROUND_SERVICE_LOCATION',
}, 'Only approved location/media/foreground-service permissions are allowed at this checkpoint'
assert permission_nodes['android.permission.READ_EXTERNAL_STORAGE'].get(A + 'maxSdkVersion') == '32', \
    'Legacy shared-storage permission must be capped at API32'

queries = manifest.find('queries')
assert queries is not None, 'Modern package visibility declarations are required'
query_packages = {item.get(A + 'name') for item in queries.findall('package')}
assert {'net.osmand.plus', 'net.osmand', 'net.osmand.dev'} <= query_packages, \
    'All supported OsmAnd package variants must be visible'
query_intents = queries.findall('intent')
assert any(
    {a.get(A + 'name') for a in intent.findall('action')} == {'android.intent.action.MAIN'}
    and {c.get(A + 'name') for c in intent.findall('category')} == {'android.intent.category.LAUNCHER'}
    for intent in query_intents
), 'Launcher app discovery must be explicitly visible without QUERY_ALL_PACKAGES'

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
assert media.get(A + 'process') is None
assert not app.findall('receiver'), 'Generic boot/ACC broadcast receivers are not approved before physical head-unit commissioning'

activity = app.find('activity')
assert activity.get(A + 'screenOrientation') == 'landscape'
assert activity.get(A + 'enableOnBackInvokedCallback') == 'false', \
    'MainActivity currently uses the official per-activity predictive-back migration opt-out'
assert all(c.get(A + 'name') != 'android.intent.category.HOME'
           for c in activity.findall('.//category')), 'Darbak must not take over the device launcher yet'
lint_config = ET.parse(ROOT / 'app/lint.xml').getroot()
ignored_lint = {item.get('id') for item in lint_config.findall('issue') if item.get('severity') == 'ignore'}
assert ignored_lint == {'GestureBackNavigation'}, \
    'Only the predictive-back lint false-positive is suppressed while the manifest opt-out is active'

build = (ROOT / 'app/build.gradle').read_text()
assert re.search(r'compileSdk\s+37\b', build), 'P10 must compile against Android 17 / API37'
assert re.search(r'minSdk\s+25\b', build), 'API25 remains the legacy regression floor'
assert re.search(r'targetSdk\s+37\b', build), 'P10 modern target must be Android 17 / API37'
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
assert 'BOOT_COMPLETED' not in java, 'Portable P10 must not assume generic boot autostart'

runtime = main / 'java/com/abosultan/darbakos/core/TripRuntimeService.java'
media_library = main / 'java/com/abosultan/darbakos/core/LocalMediaLibrary.java'
main_activity = main / 'java/com/abosultan/darbakos/MainActivity.java'
runtime_text = runtime.read_text()
main_text = main_activity.read_text()
assert runtime_text.count('new HandlerThread(') == 1
assert runtime_text.count('new Handler(') == 1
assert 'startForeground(' in runtime_text and 'NotificationChannel' in runtime_text, \
    'Modern Trip runtime must enter foreground mode on API26+'
assert 'expireIfStale(' in runtime_text, 'Trip runtime must actively expire stale GPS fixes'
assert 'fallbackToInternal(' in runtime_text, 'Trip runtime must fail over storage explicitly'
assert media_library.read_text().count('new HandlerThread(') == 1
assert media_library.read_text().count('new Handler(') == 1
assert 'scanSharedAudio(' in media_library.read_text(), 'Modern shared audio must use the MediaStore path'
assert (main / 'java/com/abosultan/darbakos/core/MediaStoreAudioScanner.java').is_file()
assert 'LocationPermissionPolicy.requestPermissions()' in main_text
assert 'StartupCoordinator.startPortableRuntime(' in main_text
assert 'MediaPermissionPolicy.requiredPermission(' in main_text

for path in main.rglob('*.java'):
    source = path.read_text()
    assert not re.search(r'new\s+(?:Thread|Timer)\s*\(|ExecutorService|Executors\.', source), path
    if path not in (runtime, media_library):
        assert not re.search(r'new\s+(?:HandlerThread|Handler)\s*\(', source), path

print('PASS: API25 legacy floor + API37 modern target, precise-location policy, MediaStore audio, package visibility, stale-GPS expiry, storage failover, scoped back-navigation migration, no generic boot receiver')
