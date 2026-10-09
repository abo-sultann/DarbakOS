package com.abosultan.darbakos.core;

/** Optional plug-in boundary for a proven vehicle-data source. No adapter is mandatory for GDN. */
public interface VehicleSourceAdapter {
    String id();
    VehicleSnapshot snapshot();
}
