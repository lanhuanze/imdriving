package com.irefire.android.imdriving.event;

import android.content.Context;
import com.irefire.android.imdriving.R;
import com.irefire.android.imdriving.engine.DictationResult;
import com.irefire.android.imdriving.engine.Engine;
import com.irefire.android.imdriving.engine.SpeakResult;
import com.irefire.android.imdriving.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

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
        l.debug("Speak:" + askText +", returns " + result);
        if(result.result == Engine.EngineResult.OK) {
            this.setNextAction(NextAction.ACTION_DICTATE_IF_READ_NOTIFICATION);
        }else {
            l.warn("Error speak:" + askText);
            this.setNextAction(NextAction.ACTION_DONE);
        }
        return result.result == Engine.EngineResult.OK;
    }

    @Override
    public boolean speakAskIfReadMessageAgain() {
        String askText = mResourceManager.getString(R.string.new_notification_ask_if_read_again);
        SpeakResult result = mEngine.speak(askText, this);
        l.debug("Speak:" + askText +", returns " + result);
        if(result.result == Engine.EngineResult.OK) {
            this.setNextAction(NextAction.ACTION_DICTATE_IF_READ_NOTIFICATION);
        }else {
            l.warn("Error speak:" + askText);
            this.setNextAction(NextAction.ACTION_DONE);
        }
        return result.result == Engine.EngineResult.OK;
    }

    @Override
    public boolean speakAskIfReply() {
        String askText = mResourceManager.getString(R.string.new_sms_ask_if_reply);
        SpeakResult result = mEngine.speak(askText, this);
        l.debug("Speak:" + askText +", returns " + result);
        if(result.result == Engine.EngineResult.OK) {
            this.setNextAction(NextAction.ACTION_DICTATE_IF_REPLY);
        }else {
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
    public void dictateIfReply() {
        DictationResult result = mEngine.dictateText(this, Constants.LISTEN_YES_NO_TIME_OUT);
        l.debug("dictateYesOrNo returns " + result);
        if(result.result != Engine.EngineResult.OK || result.texts.isEmpty()) {
            this.setNextAction(NextAction.Sp);
            return;
        }else {
            Collections.sort(result.texts);
            String text = result.texts.get(0).getText();
            if(mResourceManager.wordNegative(text)) {
                this.setNextAction(NextAction.ACTION_NEGATIVE);
            }else if(mResourceManager.wordPositive(text)) {
                this.setNextAction(NextAction.ACTION_POSITIVE);
            }else if(mResourceManager.wordStop(text)) {
                abortingCommand = text;
                this.setNextAction(NextAction.SPEAK_ABOAT_ABORTING);
            }else {
                this.setNextAction(NextAction.ASK_IF_READ_NOTIFICATION_AGAIN);
            }
        }
    };

    public void dictateIfSent() {};
}
