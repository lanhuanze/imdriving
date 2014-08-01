package com.irefire.android.imdriving.event;

import android.service.notification.StatusBarNotification;
import android.telephony.SmsMessage;

/**
 * Created by lan on 8/1/14.
 */
public class EventSource {
    public StatusBarNotification notification;
    public SmsMessage sms;

    public EventSource(SmsMessage sms) {
        this.sms = sms;
    }

    public EventSource(StatusBarNotification sbn) {
        this.notification = sbn;
    }

    public boolean valid() {
        return sms != null || notification != null;
    }
}
