package com.irefire.android.imdriving.event;

import android.telephony.SmsMessage;
import android.text.TextUtils;
import com.irefire.android.imdriving.R;
import com.irefire.android.imdriving.engine.*;
import com.irefire.android.imdriving.utils.ContactsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.service.notification.StatusBarNotification;

import com.irefire.android.imdriving.App;
import com.irefire.android.imdriving.engine.Engine.EngineResult;
import com.irefire.android.imdriving.service.ResourceManager;
import com.irefire.android.imdriving.utils.AppSettings;
import com.irefire.android.imdriving.utils.Constants;
import com.irefire.android.imdriving.utils.NotificationUtils;
import org.w3c.dom.Text;

import java.util.Collections;

/**
 * This class is abstract class of the event.
 * 
 * @author Huanze.Lan
 * 
 */
public abstract class Event {
	protected EventStatus status = EventStatus.INIT;


	private NextAction nextAction = NextAction.SPEAK_ARRIVING_TIP;
    private NextAction currentAction = NextAction.ACTION_DONE;

	protected Context mContext = null;

	protected Engine mEngine = null;
	protected ResourceManager mResourceManager = null;
	protected AppSettings mAppSettings = null;

	/**
	 * The message first read to user. For example:
	 * "New notification from FaceBook."
	 */
	protected String tips;

    protected  String title;
    protected  String content;

    protected String abortingCommand;

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
	 * 回复的内容。
	 */
	protected String replyContent;
	
	protected int trialTimes = 0;

	public Event(Context c) {
		mContext = c;
		mEngine = SystemEngine.getInstance();
		mAppSettings = AppSettings.getInstance();
		mResourceManager = ResourceManager.getInstance();
	}

	/**
	 * 说信息到来的提示。
	 */
	public final boolean speakArrivingTip() {
		SpeakResult result = mEngine.speak(tips, this);
		l.debug("speakArrivingTip tips:" + tips +" , returns:" + result);
		
		if(result.result == EngineResult.OK) {
			if(this.autoActionable() && mAppSettings.isAutoRead()) {
				this.setNextAction(NextAction.ACTION_POSITIVE);
			}else {
				this.setNextAction(NextAction.ASK_IF_READ_NOTIFICATION);
			}
		}else {
			this.setNextAction(NextAction.ACTION_DONE);
		}
		
		return EngineResult.OK == result.result;
	}

	/**
	 * 询问是否读消息。
	 * 
	 * @return
	 */
	public abstract boolean speakAskIfReadMessage();
	
	/**
	 * 提示用户上次听取失败，是否需要再次听取答案。
	 * 
	 * @return
	 */
	public abstract boolean speakAskIfReadMessageAgain();

	/**
	 * 让用户再一次说是或否
	 * 
	 * @return
	 */
	public boolean dictateIfReadMessage() {
		this.status = EventStatus.LISTENING;
		DictationResult result = mEngine.dictateText(this,
				Constants.LISTEN_YES_NO_TIME_OUT);

		if (result.result != EngineResult.OK) {
			this.setNextAction(NextAction.ASK_IF_READ_NOTIFICATION_AGAIN);
			trialTimes ++;
			if(trialTimes >= MAX_RETIAL_TIMES) {
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
				this.setNextAction(NextAction.ACTION_POSITIVE);
				matchCommand = true;
				break;
			}
			if (mResourceManager.wordNegative(text)) {
				this.setNextAction(NextAction.ACTION_NEGATIVE);
				matchCommand = true;
				break;
			}

			if (mResourceManager.wordStop(text)) {
				this.setNextAction(NextAction.SPEAK_ABOAT_ABORTING);
                abortingCommand = text;
				matchCommand = true;
				break;
			}
		}
		
		if(!matchCommand) {
			this.setNextAction(NextAction.ASK_IF_READ_NOTIFICATION_AGAIN);
			trialTimes ++;
			if(trialTimes >= MAX_RETIAL_TIMES) {
				this.setNextAction(NextAction.SPEAK_TOO_MANY_FAILED_TRIALS);
				trialTimes = 0;
			}
			return false;
		}
		trialTimes = 0;
		return true;
	}

	/**
	 * 询问是否回复该消息，只是短信或电话挂断的时候调用。
	 * 
	 * @return
	 */
	public abstract boolean speakAskIfReply();
	
	
	public abstract boolean speakStartDictateContent();
	
	/**
	 * 识别要回复的内容。
	 * @return
	 */
	public final boolean dictateReplyContent() {
		
		return true;
	}
	

	/**
	 * 让用户确认输入的内容。
	 * 
	 * @return
	 */
	public final boolean speakAskConfirmTheContent() {
		return true;
	}
	

	/**
	 * 让用户确认是否回复
	 * 
	 * @return
	 */
	public final boolean speakAskConfirmReply() {
		return true;
	}

	public final boolean speakAskRepeatContentAgain() {
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
		l.debug("NextAction: " + this.nextAction +" -----> " + nextAction);
        currentAction = this.nextAction;
		this.nextAction = nextAction;
	}

	public void dictateYesOrNo() {
		DictationResult result = mEngine.dictateText(this, Constants.LISTEN_YES_NO_TIME_OUT);
        l.debug("dictateYesOrNo returns " + result);
        if(result.result != EngineResult.OK || result.texts.isEmpty()) {
            this.setNextAction(NextAction.ASK_IF_READ_NOTIFICATION);
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
	}

    public void dictateIfReply() {};

    public void dictateIfSent() {};

	/**
	 * 获得要回复的内容，一般为短信和电话回复的功能才有。
	 */
	public abstract void dictateContent();

	/**
	 * If app set auto read the notification, we have to farther check this one.
	 * 
	 * @return
	 */
	public abstract boolean autoActionable();

	/**
	 * Action if the user say YES.
	 */
	public abstract void positiveAction();

	/**
	 * Action if the user say NO.
	 */
	public abstract void negativeAction();

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

    public static final Event createEvent(SmsMessage sms) {
        Context c = App.getStaticContext();
        ContactsManager cm = ContactsManager.getInstance();
        ResourceManager rm = ResourceManager.getInstance();
        SmsEvent event = new SmsEvent(c);
        String phoneNumber = sms.getOriginatingAddress();
        if(TextUtils.isEmpty(phoneNumber)) {
            phoneNumber = sms.getDisplayOriginatingAddress();
        }
        String name = cm.getName(phoneNumber);

        // 找不到联系人，则直接用电话号码
        if(TextUtils.isEmpty(name)) {
            name = phoneNumber;
        }

        String content = sms.getMessageBody();
        if(TextUtils.isEmpty(content)) {
            content = sms.getDisplayMessageBody();
        }

        event.setTips(rm.getString(R.string.new_sms_tip, name));
        event.setContent(rm.getString(R.string.new_sms_from_and_content, name, content));
        return event;
    }
	
	private static final Logger l = LoggerFactory.getLogger(Event.class.getSimpleName());

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
	 * 
	 */
	public static enum NextAction {
		/** 播报新消息来到 */
		SPEAK_ARRIVING_TIP,
		/** 询问用户是否读新消息, 如果是来电，则问是否接听 */
		ASK_IF_READ_NOTIFICATION,
		/** 如果听取失败，则让用户再说一次*/
		ASK_IF_READ_NOTIFICATION_AGAIN,
		/** 听取是否读消息或是接听电话 */
		ACTION_DICTATE_IF_READ_NOTIFICATION,
        /** 听取是否回复消息*/
        ACTION_DICTATE_IF_REPLY,
        /** 听取是否发送回复的内容 */
        ACTION_DICTATE_IF_SENT,
		/** 用户选择读取消息或是接听电话 */
		ACTION_POSITIVE,
		/** 用户选择不读消息或不接听电话 */
		ACTION_NEGATIVE,
		/** 询问用户是否回复信息，（电话只有拒接了才询问，短信只有读了才回复) */
		SPEAK_ASK_IF_REPLY,
		/** 告诉用户无法回复，比如我们的程序没有设置为默认的短信程序 */
		SPEAK_UNABLE_TO_REPLY,
		/** 提示用户开始听用户的回复的内容 */
		SPEAK_DICTATE_REPLY_CONTENT_START,
		/** 提示用户没有听到任何内容，然后询问是否重说一遍 */
		SPEAK_DICTATE_REPLY_CONTENT_FAILED,
		/** 提示用户上次听取失败，重新开始听用户的回复的内容 */
		SPEAK_DICTATE_REPLY_CONTENT_START_AGAIN,
		/** 重复一遍用户输入的内容 */
		SPEAK_REPEAT_REPLY_CONTENT,
		/** 询问用户是否确定回复 */
		ASK_IF_SENT_REPLY,
		/** 上次听取失败，再次询问用户是否确定回复 */
		ASK_IF_SENT_REPLY_AGAIN,
		/** 开始回复 */
		ACTION_REPLY,
		/** 发送成功 */
		SPEAK_REPLY_OK,
		/** 发送失败 */
		SPEAK_REPLY_FAILED,
		/** 程序即将退出 */
		SPEAK_ABOAT_ABORTING,
		/** 失败次数太多 */
		SPEAK_TOO_MANY_FAILED_TRIALS,
		/** 操作完成 */
		ACTION_DONE;
	}
	
	/**
	 * 每个听取失败后重试的次数.
	 */
	protected static final int MAX_RETIAL_TIMES = 3;

	public boolean speakUnableToReply() {
		// TODO Auto-generated method stub
		return true;
	}

	public void speakDictateReplyContentStart() {
		// TODO Auto-generated method stub
		
	}

	public void speakDictateReplyContentFailed() {
		// TODO Auto-generated method stub
		
	}

	public void speakDictateReplyContentStartAgain() {
		// TODO Auto-generated method stub
		
	}

	public void speakRepeatReplyContent() {
		// TODO Auto-generated method stub
		
	}

	public void speakAskIfSentReply() {
		// TODO Auto-generated method stub
		
	}

	public void speakAskIfSentReplyAgain() {
		// TODO Auto-generated method stub
		
	}

	public void reply() {
		// TODO Auto-generated method stub
		
	}

	public void speakReplyOk() {
		// TODO Auto-generated method stub
		
	}

	public void speakReplyFailed() {
		// TODO Auto-generated method stub
		
	}

	public void speakAboutAborting() {
		String text = mResourceManager.getString(R.string.speak_about_aborting, abortingCommand);
        SpeakResult result = mEngine.speak(text, this);
        l.debug("speakAboutAborting text:" + text +" , returns:" + result);

        // TODO we should disable our app here.
        this.setNextAction(NextAction.ACTION_DONE);
	}

	public void speakTooManyFailedTrials() {
        String text = mResourceManager.getString(R.string.speak_too_many_trials);
        SpeakResult result = mEngine.speak(text, this);
        l.debug("speakTooManyFailedTrials text:" + text +" , returns:" + result);

        // TODO we should disable our app here.
        this.setNextAction(NextAction.ACTION_DONE);
	}
}
