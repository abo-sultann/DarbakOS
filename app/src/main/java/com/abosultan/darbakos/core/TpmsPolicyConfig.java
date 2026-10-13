package com.abosultan.darbakos.core;

/**
 * TPMS display policy values that are provisional until physical commissioning.
 *
 * These are application defaults, not OEM-authoritative Toyota limits. Keep them centralized so
 * future settings/profile work can replace them without scattering safety-relevant constants.
 */
public final class TpmsPolicyConfig {
    public static final long PROVISIONAL_FRESH_MS = 15L * 60L * 1000L;
    public static final double PROVISIONAL_LOW_PRESSURE_PSI = 25.0;

    private TpmsPolicyConfig() { }
}
