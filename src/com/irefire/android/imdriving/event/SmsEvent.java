package com.irefire.android.imdriving.event;

import android.content.Context;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import com.irefire.android.imdriving.R;
import com.irefire.android.imdriving.engine.Engine;
import com.irefire.android.imdriving.engine.SpeakResult;
import com.irefire.android.imdriving.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 8/1/14.
 */
public class SmsEvent extends Event {
    private static final Logger l = LoggerFactory.getLogger(SmsEvent.class.getSimpleName());

    private SmsManager smsManager = SmsManager.getDefault();

    public SmsEvent(Context c) {
        super(c);
    }

    private String fromPhoneNumber;

    public void setFromPhoneNumber(String number) {
        this.fromPhoneNumber = number;
    }

    @Override
    public boolean speakAskIfReadNotification() {

        String askText = mResourceManager.getString(R.string.new_notification_ask_if_read);
        return speakAndGotoNextAction(askText, NextAction.ACTION_DICTATE_IF_READ_NOTIFICATION, NextAction.ACTION_DONE);
    }

    @Override
    public boolean speakAskIfReadNotificationAgain() {
        String askText = mResourceManager.getString(R.string.new_notification_ask_if_read_again);
        return speakAndGotoNextAction(askText, NextAction.ACTION_DICTATE_IF_READ_NOTIFICATION, NextAction.ACTION_DONE);
    }

    @Override
    public boolean speakAskIfReply() {
        String askText = mResourceManager.getString(R.string.new_sms_ask_if_reply);
        return speakAndGotoNextAction(askText, NextAction.ACTION_DICTATE_IF_REPLY, NextAction.ACTION_DONE);
    }

    @Override
    public boolean speakDictateReplyContentStart(){
        String askText = mResourceManager.getString(R.string.new_sms_dictate_content_start);
        return speakAndGotoNextAction(askText, NextAction.ACTION_DICTATE_REPLY_CONTENT, NextAction.ACTION_DONE);
    };

    @Override
    public boolean speakDictateReplyContentStartAgain(){
        String askText = mResourceManager.getString(R.string.new_sms_dictate_content_start_again);
        return speakAndGotoNextAction(askText, NextAction.ACTION_DICTATE_REPLY_CONTENT, NextAction.ACTION_DONE);
    };

    @Override
    public boolean actionDictateReplyContent() {
        String text = dictateTextWithTimeout(Constants.SPEAK_TIME_OUT, NextAction.SPEAK_REPEAT_REPLY_CONTENT, NextAction.SPEAK_DICTATE_REPLY_CONTENT_START_AGAIN);
        if(!TextUtils.isEmpty(text)) {
            replyContent = text;
            return true;
        }
        return false;
    }

    @Override
    public boolean autoActionable() {
        return false;
    }

    @Override
    public boolean actionReadNotification() {
        String readText = this.getContent();
        SpeakResult result = mEngine.speak(readText, this);
        l.debug("Speak:" + readText +", returns " + result);
        this.setNextAction(NextAction.SPEAK_ASK_IF_REPLY);
        return true;
    }

    @Override
    public boolean actionIgnoreNotification() {
        this.setNextAction(NextAction.ACTION_DONE);
        return true;
    }

    @Override
    public boolean actionDictateIfReply() {
        return dictateYesOrNo(NextAction.SPEAK_DICTATE_REPLY_CONTENT_START, NextAction.ACTION_DONE, NextAction.SPEAK_ASK_IF_REPLY_AGAIN);
    }

    @Override
    public boolean actionDictateIfSendReply() {
        return dictateYesOrNo(NextAction.ACTION_SEND_REPLY, NextAction.ACTION_DONE, NextAction.SPEAK_ASK_IF_SENT_REPLY_AGAIN);
    }

    public boolean speakAskIfReplyAgain(){
        String askText = mResourceManager.getString(R.string.new_sms_ask_if_send_reply_again);
        return speakAndGotoNextAction(askText, NextAction.ACTION_DICTATE_REPLY_CONTENT, NextAction.ACTION_DONE);
    };

    public boolean speakRepeatReplyContent(){
        if(!TextUtils.isEmpty(replyContent)) {
            String speakText = mResourceManager.getString(R.string.new_sms_repeat_reply_content, replyContent);
            return speakAndGotoNextAction(speakText, NextAction.SPEAK_ASK_IF_SENT_REPLY, NextAction.ACTION_DONE);
        }else {
            String askText = mResourceManager.getString(R.string.new_sms_reply_content_is_empty);
            return speakAndGotoNextAction(askText, NextAction.ACTION_DICTATE_REPLY_CONTENT, NextAction.ACTION_DONE);
        }
    }
    public boolean speakAskIfSendReply() {

        String askText = mResourceManager.getString(R.string.new_sms_ask_if_send_reply);
        return speakAndGotoNextAction(askText, NextAction.ACTION_DICTATE_IF_SENT_REPLY, NextAction.ACTION_DONE);
    }
    public boolean speakAskIfSendReplyAgain() {
        String askText = mResourceManager.getString(R.string.new_sms_ask_if_send_reply_again);
        return speakAndGotoNextAction(askText, NextAction.ACTION_DICTATE_REPLY_CONTENT, NextAction.ACTION_DONE);
    }

    public boolean actionSendReply() {
       ArrayList<String> messages = smsManager.divideMessage(replyContent);
       l.debug("Send message:" + replyContent +" to :" + fromPhoneNumber);
       l.debug("Message divided in " + messages.size() +" parts");
        boolean replyStatus = true;
       if(messages.size() > 1) {
           smsManager.sendMultipartTextMessage(fromPhoneNumber, null, messages, null, null);
       }else if(messages.size() == 1) {
           smsManager.sendTextMessage(fromPhoneNumber, null, messages.get(0), null, null);
       }else {
           replyStatus = false;
       }

        if(replyStatus) {
            setNextAction(NextAction.SPEAK_SEND_REPLY_OK);
        }else {
            setNextAction(NextAction.SPEAK_SEND_REPLY_FAILED);
        }
        return replyStatus;
    }
    public boolean speakSendReplyOk() {
        String askText = mResourceManager.getString(R.string.new_sms_sent_ok);
        return speakAndGotoNextAction(askText, NextAction.ACTION_DONE, NextAction.ACTION_DONE);
    }

    public boolean speakSendReplyFailed() {
        String askText = mResourceManager.getString(R.string.new_sms_sent_failed);
        return speakAndGotoNextAction(askText, NextAction.ACTION_DONE, NextAction.ACTION_DONE);
    }
}
