package com.irefire.android.imdriving.event;

import android.content.Context;
import com.irefire.android.imdriving.R;
import com.irefire.android.imdriving.engine.Engine;
import com.irefire.android.imdriving.engine.SpeakResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lan on 8/1/14.
 */
public class SmsEvent extends Event {
    private static final Logger l = LoggerFactory.getLogger(SmsEvent.class.getSimpleName());

    public SmsEvent(Context c) {
        super(c);
    }

    @Override
    public boolean speakAskIfReadMessage() {

        String askText = mResourceManager.getString(R.string.new_notification_ask_if_read);
        SpeakResult result = mEngine.speak(askText, this);
        l.debug("Speak:" + askText + ", returns " + result);
        if (result.result == Engine.EngineResult.OK) {
            this.setNextAction(NextAction.ACTION_DICTATE_IF_READ_NOTIFICATION);
        } else {
            l.warn("Error speak:" + askText);
            this.setNextAction(NextAction.ACTION_DONE);
        }
        return result.result == Engine.EngineResult.OK;
    }

    @Override
    public boolean speakAskIfReadMessageAgain() {
        String askText = mResourceManager.getString(R.string.new_notification_ask_if_read_again);
        SpeakResult result = mEngine.speak(askText, this);
        l.debug("Speak:" + askText + ", returns " + result);
        if (result.result == Engine.EngineResult.OK) {
            this.setNextAction(NextAction.ACTION_DICTATE_IF_READ_NOTIFICATION);
        } else {
            l.warn("Error speak:" + askText);
            this.setNextAction(NextAction.ACTION_DONE);
        }
        return result.result == Engine.EngineResult.OK;
    }

    @Override
    public boolean speakAskIfReply() {
        String askText = mResourceManager.getString(R.string.new_sms_ask_if_reply);
        SpeakResult result = mEngine.speak(askText, this);
        l.debug("Speak:" + askText + ", returns " + result);
        if (result.result == Engine.EngineResult.OK) {
            this.setNextAction(NextAction.ACTION_DICTATE_IF_REPLY);
        } else {
            l.warn("Error speak:" + askText);
            this.setNextAction(NextAction.ACTION_DONE);
        }
        return result.result == Engine.EngineResult.OK;
    }

    @Override
    public boolean speakStartDictateContent() {
        return false;
    }

    @Override
    public boolean dictateContent() {
        return true;
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

    @Override
    public boolean dictateIfReply() {
        return dictateYesOrNo(NextAction.SPEAK_DICTATE_REPLY_CONTENT_START, NextAction.ACTION_DONE, NextAction.ACTION_DICTATE_IF_REPLY);
    }

    public boolean dictateIfSent() {
        return dictateYesOrNo(NextAction.ACTION_SEND_REPLY, NextAction.ACTION_DONE, NextAction.);
    }

}
