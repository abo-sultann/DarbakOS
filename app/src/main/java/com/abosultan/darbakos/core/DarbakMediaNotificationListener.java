package com.abosultan.darbakos.core;

import android.service.notification.NotificationListenerService;

/**
 * Permission anchor only. GDN does not inspect or persist notifications here; enabling this
 * component lets MediaSessionManager expose active media sessions from other apps.
 */
public final class DarbakMediaNotificationListener extends NotificationListenerService {
}
