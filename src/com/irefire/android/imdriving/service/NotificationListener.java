package com.irefire.android.imdriving.service;

import com.irefire.android.imdriving.event.EventSource;
import com.irefire.android.imdriving.utils.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.irefire.android.imdriving.utils.Constants;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationListener extends NotificationListenerService {

    private AppSettings mAppSettings = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppSettings = AppSettings.getInstance();
        l.debug("onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        l.debug("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private static final Logger l = LoggerFactory.getLogger(NotificationListener.class.getSimpleName());
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		if(sbn.getPackageName().equals(this.getBaseContext().getPackageName())) {
			// If we receive a notification sent by ourselves.
			// we think the user had enabled our app to receive the notification.
			Intent intent = new Intent();
			intent.setAction(Constants.NOTIFICATION_ENABLED);
			sendBroadcast(intent);
			l.debug("User had enabled notification receiving. Send an intent.");
			return;
		}

        if(mAppSettings.allowedReadNotification(sbn.getPackageName())) {
            l.debug("new notification received:" + sbn.getPackageName());
            NotificationProcessor.getInstance().enqueueEventSource(new EventSource(sbn));
        }else {
            l.debug("We will ignore notification from " + sbn.getPackageName());
            return;
        }


	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		l.debug("notification removed:" + sbn.toString());
	}

}
