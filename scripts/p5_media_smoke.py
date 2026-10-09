#!/usr/bin/env python3
"""Bounded API25 verification for GDN P5 external MediaSession foundation."""
from pathlib import Path
import hashlib
import json
import re
import subprocess
import time
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'test-evidence'
OUT.mkdir(exist_ok=True)
PACKAGE = 'com.abosultan.darbakos.test'
ACTIVITY = PACKAGE + '/com.abosultan.darbakos.MainActivity'
LISTENER = PACKAGE + '/com.abosultan.darbakos.core.DarbakMediaNotificationListener'
FIXTURE_PACKAGE = 'com.abosultan.darbakos.mediafixture'
FIXTURE_ACTIVITY = FIXTURE_PACKAGE + '/.FixtureActivity'
serials = [line.split()[0] for line in subprocess.check_output(['adb', 'devices'], text=True).splitlines()[1:] if line.endswith('\tdevice') and line.startswith('emulator-')]
assert len(serials) == 1
ADB = ['adb', '-s', serials[0]]

def adb(*args, binary=False): return subprocess.check_output(ADB + list(args), text=not binary, timeout=60)
def save(name, value):
    path = OUT / name
    path.write_bytes(value) if isinstance(value, bytes) else path.write_text(value)
def instrument(classes, expected, name):
    selected = ','.join('com.abosultan.darbakos.' + item for item in classes)
    output = subprocess.check_output(ADB + ['shell','am','instrument','-w','-e','class',selected,PACKAGE + '.test/androidx.test.runner.AndroidJUnitRunner'], text=True, timeout=180)
    save(name + '.txt', output)
    success = 'OK (' + str(expected) + (' test)' if expected == 1 else ' tests)')
    assert success in output and 'FAILURES' not in output, output
def dump_retry(name, attempts=6):
    last=''
    for attempt in range(attempts):
        subprocess.run(ADB+['shell','rm','-f','/sdcard/darbak-ui.xml'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
        result=subprocess.run(ADB+['shell','uiautomator','dump','/sdcard/darbak-ui.xml'],text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=30); last=result.stdout or ''
        try:
            xml=adb('shell','cat','/sdcard/darbak-ui.xml'); tree=ET.fromstring(xml); save(name+'.xml',xml); save(name+'-dump.txt',last); return tree
        except (subprocess.CalledProcessError,ET.ParseError): time.sleep(0.7+attempt*0.2)
    save(name+'-dump-failure.txt',last+'\n'+adb('shell','dumpsys','window','windows')); raise AssertionError('uiautomator could not capture Settings after retries')
def center(node):
    match=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',node.get('bounds','')); assert match,node.get('bounds',''); l,t,r,b=map(int,match.groups()); return (l+r)//2,(t+b)//2
def tap_node(node):
    x,y=center(node); adb('shell','input','tap',str(x),str(y)); time.sleep(0.7)
def listener_setting(): return adb('shell','settings','get','secure','enabled_notification_listeners').strip()
def grant_listener_via_settings():
    launch=adb('shell','am','start','-W','-a','android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS'); save('media-listener-settings-launch.txt',launch); time.sleep(1.2)
    tree=dump_retry('media-listener-settings'); candidates=[]
    for item in tree.iter('node'):
        text=item.get('text',''); desc=item.get('content-desc','')
        if any(label in text or label in desc for label in ('غضن','GDN','دربك','Darbak')): candidates.append(item)
    assert candidates, 'GDN media access row not found in Notification access Settings'
    candidates.sort(key=lambda item:(item.get('text','')=='',-len(item.get('text','')))); tap_node(candidates[0])
    for attempt in range(8):
        enabled=listener_setting()
        if 'DarbakMediaNotificationListener' in enabled:
            save('media-listener-enabled.txt',enabled); adb('shell','am','force-stop','com.android.settings'); time.sleep(1.2); return
        tree=dump_retry('media-listener-confirm-'+str(attempt),attempts=3)
        positive=[n for n in tree.iter('node') if n.get('resource-id','').endswith(':id/button1') or n.get('resource-id','')=='android:id/button1']
        if positive: tap_node(positive[0])
        time.sleep(0.5)
    save('media-listener-enable-failure.txt',listener_setting()); raise AssertionError('Notification-listener access did not become enabled through Settings UI')
def screen(name):
    png=adb('exec-out','screencap','-p',binary=True); assert png[:8]==b'\x89PNG\r\n\x1a\n'; assert (int.from_bytes(png[16:20],'big'),int.from_bytes(png[20:24],'big'))==(1024,600); save(name+'.png',png)

try:
    save('bootstrap.txt','serial='+serials[0]+'\n'); assert adb('shell','getprop','ro.build.version.sdk').strip()=='25'; adb('shell','wm','size','1024x600'); adb('shell','wm','density','160')
    apk=ROOT/'app/build/outputs/apk/debug/app-debug.apk'; test_apk=ROOT/'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'; fixture_apks=list((ROOT/'mediafixture/build/outputs/apk/debug').glob('*.apk')); assert len(fixture_apks)==1,fixture_apks; fixture_apk=fixture_apks[0]
    adb('install','-r',str(apk)); adb('install','-r','-t',str(test_apk)); adb('install','-r',str(fixture_apk)); adb('shell','pm','grant',PACKAGE,'android.permission.ACCESS_FINE_LOCATION'); adb('shell','settings','delete','secure','enabled_notification_listeners'); adb('logcat','-c')
    instrument(['ShellTest#launchIsArabicLandscapeAndSpeedStateIsTruthful','ShellTest#everyDestinationAndBackWorks','ShellTest#recreatePreservesSectionAndHomeButtonWorks','ShellTest#homeFits1024x600AndRtlNavigation','ShellTest#mapSurfaceFitsAndAbsentEngineStaysTruthful','ShellTest#mediaSurfaceFitsAndNeverOffersAutoplayWithoutAccess','MediaSnapshotTest','LocalMediaIntegrationTest','LocalMediaPlaybackTest'],10,'media-no-access-instrumentation')
    launch=adb('shell','am','start','-W','-n',ACTIVITY); save('media-launch-no-access.txt',launch); assert 'Status: ok' in launch; subprocess.check_call(ADB+['emu','geo','fix','46.6753','24.7136']); time.sleep(0.8); services=adb('shell','dumpsys','activity','services',PACKAGE); save('media-trip-service.txt',services); assert 'TripRuntimeService' in services and 'startRequested=true' in services; screen('media-home-no-access-1024x600')
    adb('shell','am','force-stop',PACKAGE); grant_listener_via_settings()
    fixture_launch=adb('shell','am','start','-W','-n',FIXTURE_ACTIVITY); save('media-fixture-launch.txt',fixture_launch); assert 'Status: ok' in fixture_launch; time.sleep(0.8); before=adb('shell','dumpsys','media_session'); save('media-session-before-gdn.txt',before); assert 'DarbakP5Fixture' in before
    instrument(['ExternalMediaIntegrationTest'],1,'external-media-integration'); after=adb('shell','dumpsys','media_session'); save('media-session-after-control.txt',after); assert 'DarbakP5Fixture' in after
    adb('shell','am','force-stop',FIXTURE_PACKAGE); time.sleep(0.8); instrument(['MediaAccessShellTest'],1,'media-access-idle-instrumentation'); sessions=adb('shell','dumpsys','media_session'); save('media-session-after-fixture-stop.txt',sessions); assert 'DarbakP5Fixture' not in sessions; adb('shell','am','start','-W','-n',ACTIVITY); time.sleep(0.8); screen('media-home-access-idle-1024x600')
    save('summary.json',json.dumps({'result':'PASS','commit':subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip(),'api':25,'abi':adb('shell','getprop','ro.product.cpu.abi').strip(),'resolution':'1024x600','density':160,'notification_listener_component':LISTENER,'permission_grant_path':'Android Settings notification access UI','no_autoplay_proven':True,'explicit_transport_proven':True,'trip_runtime_alive':True,'t3_validated':False},ensure_ascii=False,indent=2)); print('PASS: P5 media gate; GDN identity + external session + local media')
finally:
    crash=adb('logcat','-b','crash','-d'); logs=adb('logcat','-d'); save('final-crash.txt',crash); save('final-logcat.txt',logs)
    try: adb('shell','am','force-stop',FIXTURE_PACKAGE); adb('shell','settings','delete','secure','enabled_notification_listeners')
    except Exception: pass
    assert 'FATAL EXCEPTION' not in crash,crash; assert 'ANR in '+PACKAGE not in logs
