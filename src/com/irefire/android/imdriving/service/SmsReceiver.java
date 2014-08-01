package com.irefire.android.imdriving.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by lan on 8/1/14.
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final Logger l = LoggerFactory.getLogger(SmsReceiver.class.getSimpleName());

    public static SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        if(messages == null && intent.getExtras() != null) {
            messages = (Object[])intent.getExtras().get("pdus");
        }
        String format = intent.getStringExtra("format");

        int pduCount = messages.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];

        for (int i = 0; i < pduCount; i++) {
            byte[] pdu = (byte[]) messages[i];

            msgs[i] = createFromPdu(pdu, format);
        }
        return msgs;
    }

    private static Method createFromPduMethod = null;

    private static SmsMessage createFromPdu(byte[] pdu, String format) {
        SmsMessage message = null;
        if (TextUtils.isEmpty(format)) {
            message = SmsMessage.createFromPdu(pdu);
        } else {
            Class clz = SmsMessage.class;

            try {
                if(createFromPduMethod == null) {
                    createFromPduMethod = clz.getDeclaredMethod("createFromPdu", byte[].class, String.class);
                }
                if (createFromPduMethod != null) {
                    createFromPduMethod.setAccessible(true);
                    message = (SmsMessage) createFromPduMethod.invoke(null, pdu, format);
                } else {
                    message = SmsMessage.createFromPdu(pdu);
                }
            } catch (NoSuchMethodException e) {

            } catch (IllegalAccessException e) {

            } catch (InvocationTargetException e) {

            }
        }
        return message;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            NotificationProcessor np = NotificationProcessor.getInstance();
            SmsMessage[] messages = getMessagesFromIntent(intent);
            for(SmsMessage sms: messages) {
                l.debug("enqueueEvent sms:" + sms);
                np.enqueueEvent(sms);
            }
        }
    }


}
