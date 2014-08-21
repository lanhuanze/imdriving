package com.irefire.android.imdriving.event;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import com.irefire.android.imdriving.App;
import com.irefire.android.imdriving.R;
import com.irefire.android.imdriving.engine.*;
import com.irefire.android.imdriving.engine.Engine.EngineResult;
import com.irefire.android.imdriving.service.ResourceManager;
import com.irefire.android.imdriving.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * This class is abstract class of the event.
 *
 * @author Huanze.Lan
 */
public abstract class Event {
    /**
     * 每个听取失败后重试的次数.
     */
    protected static final int MAX_RETRIAL_TIMES = 3;
    private static final Logger l = LoggerFactory.getLogger(Event.class.getSimpleName());
    protected EventStatus status = EventStatus.INIT;
    protected Context mContext = null;

    protected Engine mEngine = null;
    protected ResourceManager mResourceManager = null;
    protected AppSettings mAppSettings = null;

    /**
     * The message first read to user. For example:
     * "New notification from FaceBook."
     */
    protected String tips;

    protected String title;
    protected String content;

    protected String abortingCommand;
    /**
     * 回复的内容。
     */
    protected String replyContent;
    protected int trialTimes = 0;
    private NextAction nextAction = NextAction.SPEAK_ARRIVING_TIP;
    private NextAction currentAction = NextAction.ACTION_DONE;

    public Event(Context c) {
        mContext = c;
        mEngine = SystemEngine.getInstance();
        mAppSettings = AppSettings.getInstance();
        mResourceManager = ResourceManager.getInstance();
    }

    public static final Event createEvent(StatusBarNotification sbn) {
        ResourceManager rm = ResourceManager.getInstance();
        String name = ResourceManager.getInstance().getAppName(
                sbn.getPackageName());
        boolean autoRead = AppSettings.getInstance().isAutoRead();
        Context c = App.getStaticContext();
        Event event = new NotificationEvent(c);
        String title = NotificationUtils.getTitle(sbn.getNotification());
        String content = NotificationUtils.getContent(sbn.getNotification());
        event.setTips(rm.getString(R.string.new_notification_tip, name));
        event.setTitle(title);
        event.setContent(content);
        return event;
    }

    public static final Event createEvent(ShortMessage sms) {
        Context c = App.getStaticContext();
        ContactsManager cm = ContactsManager.getInstance();
        ResourceManager rm = ResourceManager.getInstance();
        SmsEvent event = new SmsEvent(c);

        String phoneNumber = sms.getFromAddress();
        event.setFromPhoneNumber(phoneNumber);

        String name = cm.getName(phoneNumber);

        // 找不到联系人，则直接用电话号码
        if (TextUtils.isEmpty(name)) {
            name = phoneNumber;
        }

        name = Systems.formatPhoneNumber(name);

        event.setTips(rm.getString(R.string.new_sms_tip, name));
        event.setContent(rm.getString(R.string.new_sms_from_and_content, name, sms.getBody()));
        return event;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 说信息到来的提示。
     */
    public final boolean speakArrivingTip() {
        SpeakResult result = mEngine.speak(tips, this);
        l.debug("speakArrivingTip tips:" + tips + " , returns:" + result);

        if (result.result == EngineResult.OK) {
            if (this.autoActionable() && mAppSettings.isAutoRead()) {
                this.setNextAction(NextAction.ACTION_READ_NOTIFICATION);
            } else {
                this.setNextAction(NextAction.SPEAK_ASK_IF_READ_NOTIFICATION);
            }
        } else {
            this.setNextAction(NextAction.ACTION_DONE);
        }

        return EngineResult.OK == result.result;
    }

    public abstract boolean speakAskIfReadNotification();

    public abstract boolean speakAskIfReadNotificationAgain();

    ;

    public boolean actionDictateIfReadMessage() {
        return dictateYesOrNo(NextAction.ACTION_READ_NOTIFICATION, NextAction.ACTION_IGNORE_NOTIFICATION, NextAction.SPEAK_ASK_IF_READ_NOTIFICATION_AGAIN);
    }

    ;

    public abstract boolean actionReadNotification();

    ;

    public abstract boolean actionIgnoreNotification();

    ;

    public boolean speakAskIfReply() {
        throw new UnsupportedOperationException("Subclass should override it.");
    }

    ;

    public boolean speakAskIfReplyAgain() {
        throw new UnsupportedOperationException("Subclass should override it.");
    }

    public boolean actionDictateIfReply() {
        throw new UnsupportedOperationException("Subclass should override it.");
    }

    public boolean speakDictateReplyContentStart() {
        throw new UnsupportedOperationException("Subclass should override it.");
    }

    public boolean speakDictateReplyContentStartAgain() {
        throw new UnsupportedOperationException("Subclass should override it.");
    }

    public boolean actionDictateReplyContent() {
        throw new UnsupportedOperationException("Subclass should override it.");
    }

    public boolean speakRepeatReplyContent() {
        throw new UnsupportedOperationException("Subclass should override it.");
    }

    public boolean speakAskIfSendReply() {
        throw new UnsupportedOperationException("Subclass should override it.");
    }

    public boolean speakAskIfSendReplyAgain() {
        throw new UnsupportedOperationException("Subclass should override it.");
    }

    public boolean actionDictateIfSendReply() {
        throw new UnsupportedOperationException("Subclass should override it.");
    }

    public boolean actionSendReply() {
        throw new UnsupportedOperationException("Subclass should override it.");
    }

    public boolean speakSendReplyOk() {
        throw new UnsupportedOperationException("Subclass should override it.");
    }

    public boolean speakSendReplyFailed() {
        throw new UnsupportedOperationException("Subclass should override it.");
    }

    public boolean speakAboutAborting() {
        String text = mResourceManager.getString(R.string.speak_about_aborting, abortingCommand);
        SpeakResult result = mEngine.speak(text, this);
        l.debug("speakAboutAborting text:" + text + " , returns:" + result);

        // TODO we should disable our app here.
        this.setNextAction(NextAction.ACTION_DONE);
        return true;
    }

    public boolean speakTooManyFailedTrials() {
        String text = mResourceManager.getString(R.string.speak_too_many_trials);
        SpeakResult result = mEngine.speak(text, this);
        l.debug("speakTooManyFailedTrials text:" + text + " , returns:" + result);

        // TODO we should disable our app here.
        this.setNextAction(NextAction.ACTION_DONE);
        return true;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public EventStatus getEventStatus() {
        return status;
    }

    protected void setEventStatus(EventStatus status) {
        this.status = status;
    }

    public NextAction getNextAction() {
        return nextAction;
    }

    protected final void setNextAction(NextAction nextAction) {
        l.debug("NextAction: " + this.nextAction + " -----> " + nextAction);
        currentAction = this.nextAction;
        this.nextAction = nextAction;
    }

    /**
     * If app set auto read the notification, we have to farther check this one.
     *
     * @return
     */
    public abstract boolean autoActionable();

    protected final boolean dictateYesOrNo(NextAction positiveAction, NextAction negativeAction, NextAction againAction) {
        this.status = EventStatus.LISTENING;
        DictationResult result = mEngine.dictateText(this,
                Constants.LISTEN_YES_NO_TIME_OUT);

        if (result.result != EngineResult.OK) {
            this.setNextAction(againAction);
            trialTimes++;
            if (trialTimes >= MAX_RETRIAL_TIMES) {
                this.setNextAction(NextAction.SPEAK_TOO_MANY_FAILED_TRIALS);
                trialTimes = 0;
            }
            return false;
        }
        String text = null;
        boolean matchCommand = false;
        for (ResultText t : result.texts) {
            text = t.getText().toUpperCase(mAppSettings.getLocale());
            if (mResourceManager.wordPositive(text)) {
                this.setNextAction(positiveAction);
                matchCommand = true;
                break;
            }
            if (mResourceManager.wordNegative(text)) {
                this.setNextAction(negativeAction);
                matchCommand = true;
                break;
            }

            if (mResourceManager.wordStop(text)) {
                this.setNextAction(NextAction.SPEAK_ABOUT_ABORTING);
                abortingCommand = text;
                matchCommand = true;
                break;
            }
        }

        if (!matchCommand) {
            this.setNextAction(againAction);
            trialTimes++;
            if (trialTimes >= MAX_RETRIAL_TIMES) {
                this.setNextAction(NextAction.SPEAK_TOO_MANY_FAILED_TRIALS);
                trialTimes = 0;
            }
            return false;
        }
        trialTimes = 0;
        return true;
    }

    protected final boolean speakAndGotoNextAction(String text, NextAction okAction, NextAction failedAction) {
        SpeakResult result = mEngine.speak(text, this);
        l.debug("Speak:" + text + ", returns " + result);
        if (result.result == Engine.EngineResult.OK) {
            this.setNextAction(okAction);
        } else {
            l.warn("Error speak:" + text);
            this.setNextAction(failedAction);
        }
        return result.result == Engine.EngineResult.OK;
    }

    protected final String dictateTextWithTimeout(long timeout, NextAction okAction, NextAction againAction) {
        this.status = EventStatus.LISTENING;
        DictationResult result = mEngine.dictateText(this,
                timeout);

        if (result.result != EngineResult.OK) {
            this.setNextAction(againAction);
            trialTimes++;
            if (trialTimes >= MAX_RETRIAL_TIMES) {
                this.setNextAction(NextAction.SPEAK_TOO_MANY_FAILED_TRIALS);
                trialTimes = 0;
                return "";
            }
        }
        boolean matchCommand = false;
        if (result.texts.size() > 0) {
            Collections.sort(result.texts);
            matchCommand = true;
            trialTimes = 0;
            this.setNextAction(okAction);
            return result.texts.get(0).getText();
        }
        if (!matchCommand) {
            this.setNextAction(againAction);
            trialTimes++;
            if (trialTimes >= MAX_RETRIAL_TIMES) {
                this.setNextAction(NextAction.SPEAK_TOO_MANY_FAILED_TRIALS);
                trialTimes = 0;
            }
        }
        return "";
    }

    /**
     * Event 当时的状态。
     */
    public static enum EventStatus {
        INIT, SPEAKING, LISTENING, DONE_OK, DONE_ERROR;
    }


    /**
     * What next step should event do.
     *
     * @author lan
     */
    public static enum NextAction {
        /**
         * 播报新消息来到
         */
        SPEAK_ARRIVING_TIP,
        /**
         * 询问用户是否读新消息, 如果是来电，则问是否接听
         */
        SPEAK_ASK_IF_READ_NOTIFICATION,
        /**
         * 如果听取失败，则让用户再说一次
         */
        SPEAK_ASK_IF_READ_NOTIFICATION_AGAIN,
        /**
         * 听取是否读消息或是接听电话
         */
        ACTION_DICTATE_IF_READ_NOTIFICATION,
        /**
         * 用户选择读取消息或是接听电话
         */
        ACTION_READ_NOTIFICATION,
        /**
         * 用户选择不读消息或不接听电话
         */
        ACTION_IGNORE_NOTIFICATION,
        /**
         * 询问用户是否回复信息，（电话只有拒接了才询问，短信只有读了才回复)
         */
        SPEAK_ASK_IF_REPLY,
        /**
         * 提示用户重新说一遍是否回复
         */
        SPEAK_ASK_IF_REPLY_AGAIN,
        /**
         * 听取是否回复消息
         */
        ACTION_DICTATE_IF_REPLY,
        /**
         * 提示用户开始听用户的回复的内容
         */
        SPEAK_DICTATE_REPLY_CONTENT_START,
        /**
         * 提示用户上次听取失败，重新开始听用户的回复的内容
         */
        SPEAK_DICTATE_REPLY_CONTENT_START_AGAIN,
        /**
         * 开始听取回复内容
         */
        ACTION_DICTATE_REPLY_CONTENT,
        /**
         * 重复一遍用户输入的内容
         */
        SPEAK_REPEAT_REPLY_CONTENT,
        /**
         * 询问用户是否确定回复
         */
        SPEAK_ASK_IF_SENT_REPLY,
        /**
         * 上次听取失败，再次询问用户是否确定回复
         */
        SPEAK_ASK_IF_SENT_REPLY_AGAIN,
        /**
         * 听取是否发送回复的内容
         */
        ACTION_DICTATE_IF_SENT_REPLY,
        /**
         * 用户选择发送回复
         */
        ACTION_SEND_REPLY,
        /**
         * 发送成功
         */
        SPEAK_SEND_REPLY_OK,
        /**
         * 发送失败
         */
        SPEAK_SEND_REPLY_FAILED,
        /**
         * 程序即将退出
         */
        SPEAK_ABOUT_ABORTING,
        /**
         * 失败次数太多
         */
        SPEAK_TOO_MANY_FAILED_TRIALS,
        /**
         * 操作完成
         */
        ACTION_DONE;
    }
}
