package com.abosultan.darbakos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.os.Build;
import android.provider.MediaStore;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.abosultan.darbakos.core.DeviceCapabilityPolicy;
import com.abosultan.darbakos.core.HardwareProfile;
import com.abosultan.darbakos.core.MediaStoreAudioScanner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class MediaStoreAudioScannerCapabilityTest {
    @Test public void api29PlusUsesMergedVolumeWhenRemovableMediaIsExpected() {
        DeviceCapabilityPolicy capabilities = new DeviceCapabilityPolicy(HardwareProfile.DEFAULT);

        assertEquals(
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
                MediaStoreAudioScanner.collectionUri(Build.VERSION_CODES.Q, capabilities));
    }

    @Test public void api29PlusUsesPrimaryOnlyWhenRemovableMediaIsNotExpected() {
        HardwareProfile primaryOnly = HardwareProfile.measured(
                "primary-only-head-unit",
                37,
                1920,
                1080,
                4096,
                8192,
                false,
                true,
                true);
        DeviceCapabilityPolicy capabilities = new DeviceCapabilityPolicy(primaryOnly);

        assertEquals(
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                MediaStoreAudioScanner.collectionUri(Build.VERSION_CODES.Q, capabilities));
        assertNotEquals(
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
                MediaStoreAudioScanner.collectionUri(Build.VERSION_CODES.Q, capabilities));
    }

    @Test public void legacyApiKeepsLegacyExternalCollection() {
        HardwareProfile primaryOnly = HardwareProfile.measured(
                "legacy-primary-only",
                25,
                1024,
                600,
                2048,
                4096,
                false,
                true,
                true);
        DeviceCapabilityPolicy capabilities = new DeviceCapabilityPolicy(primaryOnly);

        assertEquals(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStoreAudioScanner.collectionUri(25, capabilities));
    }
}
