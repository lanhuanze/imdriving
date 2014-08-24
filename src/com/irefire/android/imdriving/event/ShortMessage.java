package com.irefire.android.imdriving.event;

import android.telephony.SmsMessage;
import android.text.TextUtils;

/**
 * Created by lan on 8/5/14.
 */
public class ShortMessage {
    private String from;
    private StringBuilder body = new StringBuilder();

    private ShortMessage(String from) {
        this.from = from;
    }

    public static ShortMessage from(SmsMessage[] smses) {
        assert smses != null;
        SmsMessage sms = smses[0];
        String phoneNumber = sms.getOriginatingAddress();
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumber = sms.getDisplayOriginatingAddress();
        }

        ShortMessage m = new ShortMessage(phoneNumber);

        for (SmsMessage sm : smses) {
            String content = sm.getMessageBody();
            if (TextUtils.isEmpty(content)) {
                content = sm.getDisplayMessageBody();
            }
            m.appendBody(content);
        }
        return m;
    }

    public String getFromAddress() {
        return from;
    }

    public void appendBody(String bodyPart) {
        body.append(bodyPart);
    }

    public String getBody() {
        return body.toString();
    }

    @Override
    public String toString() {
        return "ShortMessage{" +
                "from='" + from + '\'' +
                ", body=" + body +
                '}';
    }
}
