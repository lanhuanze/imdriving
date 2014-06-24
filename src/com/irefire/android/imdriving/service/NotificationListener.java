package com.irefire.android.imdriving.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationListener extends NotificationListenerService {
	private static final Logger l = LoggerFactory.getLogger(NotificationListener.class);
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		l.debug("new notification received:" + sbn.toString());
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		l.debug("notification removed:" + sbn.toString());
	}

}
