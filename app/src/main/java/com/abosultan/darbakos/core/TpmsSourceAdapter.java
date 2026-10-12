package com.abosultan.darbakos.core;

/**
 * Transport-neutral TPMS source boundary. Implementations may later use a proven USB, Wi-Fi,
 * Bluetooth, serial, RF, or other transport without changing the GDN vehicle-data contract.
 */
public interface TpmsSourceAdapter {
    String id();
    TpmsSourceSnapshot snapshot();
}
