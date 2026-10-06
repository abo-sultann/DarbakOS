package com.abosultan.darbakos;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.abosultan.darbakos.core.RuntimeStartPolicy;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class RuntimeStartPolicyTest {
    @Test public void api25UsesLegacyServiceStart() {
        assertFalse(RuntimeStartPolicy.useForegroundServiceStart(25));
    }

    @Test public void api26AndModernUseForegroundServiceStart() {
        assertTrue(RuntimeStartPolicy.useForegroundServiceStart(26));
        assertTrue(RuntimeStartPolicy.useForegroundServiceStart(35));
    }

    @Test public void activityStartRequiresVisibilityAndLocationPermission() {
        assertTrue(RuntimeStartPolicy.allowActivityStart(true, true));
        assertFalse(RuntimeStartPolicy.allowActivityStart(true, false));
        assertFalse(RuntimeStartPolicy.allowActivityStart(false, true));
        assertFalse(RuntimeStartPolicy.allowActivityStart(false, false));
    }
}
