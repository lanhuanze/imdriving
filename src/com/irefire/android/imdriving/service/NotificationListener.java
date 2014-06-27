package com.irefire.android.imdriving.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.irefire.android.imdriving.utils.Constants;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationListener extends NotificationListenerService {
	private static final Logger l = LoggerFactory.getLogger(NotificationListener.class);
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		if(sbn.getPackageName().equals(this.getBaseContext().getPackageName())) {
			// If we receive a notification sent by ourself. 
			// we think the user had enabled our app to recieve the notification.
			Intent intent = new Intent();
			intent.setAction(Constants.NOTIFICATION_ENABLED);
			sendBroadcast(intent);
			l.debug("User had enabled notification receiving. Send an intent.");
			return;
		}
		l.debug("new notification received:" + sbn.toString());
		NotifcationProcessor.getInstance().enqueueEvent(sbn);
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		l.debug("notification removed:" + sbn.toString());
	}

}
