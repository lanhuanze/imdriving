package com.irefire.android.imdriving.event;

import android.content.Context;

/**
 * Created by lan on 8/1/14.
 */
public class SmsEvent extends Event {
    public SmsEvent(Context c) {
        super(c);
    }
    @Override
    public boolean speakAskIfReadMessage() {
        return false;
    }

    @Override
    public boolean speakAskIfReadMessageAgain() {
        return false;
    }

    @Override
    public boolean speakAskIfReply() {
        return false;
    }

    @Override
    public boolean speakStartDictateContent() {
        return false;
    }

    @Override
    public void dictateContent() {

    }

    @Override
    public boolean autoActionable() {
        return false;
    }

    @Override
    public void positiveAction() {

    }

    @Override
    public void negativeAction() {

    }
}
