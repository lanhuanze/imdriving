package com.irefire.android.imdriving.event;

import android.content.Context;

/**
 * Created by lan on 7/23/14.
 */
public class DummyEvent extends Event {


    public DummyEvent(Context c) {
        super(c);
    }

    @Override
    public boolean speakAskIfReadNotification() {
        return false;
    }

    @Override
    public boolean speakAskIfReadNotificationAgain() {
        return false;
    }

    @Override
    public boolean actionReadNotification() {
        return false;
    }

    @Override
    public boolean actionIgnoreNotification() {
        return false;
    }

    @Override
    public boolean autoActionable() {
        return false;
    }


}
