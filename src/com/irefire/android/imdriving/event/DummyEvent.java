package com.irefire.android.imdriving.event;

import android.content.Context;

/**
 * Created by lan on 7/23/14.
 */
public class DummyEvent extends Event {
    @Override
    public boolean speakAskIfReadMessage() {
        return false;
    }

    public DummyEvent(Context c) {
        super(c);
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
