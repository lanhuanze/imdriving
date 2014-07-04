package com.irefire.android.imdriving.event;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.service.notification.StatusBarNotification;

import com.irefire.android.imdriving.App;
import com.irefire.android.imdriving.R;
import com.irefire.android.imdriving.service.ResourceManager;
import com.irefire.android.imdriving.service.Engine;
import com.irefire.android.imdriving.service.ResultText;
import com.irefire.android.imdriving.utils.AppSettings;
import com.irefire.android.imdriving.utils.Constants;
import com.irefire.android.imdriving.utils.NotificationUtils;

/**
 * This class is abstract class of the event.
 * @author Huanze.Lan
 *
 */
public abstract class Event {
	
	private static final Logger l = LoggerFactory.getLogger(Event.class);
	
	public static enum EventStatus {
		INIT, SPEAKING, LISTENING, DONE_OK, DONE_ERROR;
	}
	
	/**
	 * What next step should event do.
	 * @author lan
	 *
	 */
	public static enum NextAction {
		INIT, YES, NO, TRYAGAIN, REPLY, ABORT, DONE, DICTATION_YES_OR_NO, DICTATION_CONTENT;
	}
	
	protected Context mContext = null;
	
	protected Engine mEngine = null;
	protected ResourceManager mResourceManager = null;
	
	/**
	 * The message first read to user. 
	 * For example: "New notification from FaceBook."
	 */
	protected String tips;
	
	/**
	 * The message to ask user to choose next step.
	 * For example: "Read it? Please say YES or NO."
	 */
	protected String questionToAsk;
	
	/**
	 * The message title.
	 * For example: 
	 */
	protected String title;
	
	
	protected String content;
	
	/**
	 * 
	 */
	protected EventStatus status = EventStatus.INIT;
	
	protected List<ResultText> mRecognitionResult;
	
	/**
	 * Speak the suggestion after we can dictate from user.
	 */
	protected String suggestion;
	
	protected NextAction nextAction = NextAction.INIT;
	
	public Event(Context c) {
		mContext = c;
		mEngine = Engine.getInstance();
		mResourceManager = ResourceManager.getInstance();q
	}

	public String getTips() {
		return tips;
	}

	public void setTips(String tips) {
		this.tips = tips;
	}

	public String getQuestionToAsk() {
		return questionToAsk;
	}

	public void setQuestionToAsk(String questionToAsk) {
		this.questionToAsk = questionToAsk;
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
	
	public EventStatus getEventStatus() {
		return status;
	}
	
	public void setEventStatus(EventStatus status) {
		this.status = status;
	}
	
	
	
	public List<ResultText> getRecognitionResult() {
		return Collections.unmodifiableList(mRecognitionResult);
	}

	public void setRecognitionResult(List<ResultText> mRecognitionResult) {
		this.mRecognitionResult = mRecognitionResult;
	}

	
	
	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
	}
	
	public NextAction getNextAction() {
		return nextAction;
	}

	public void setNextAction(NextAction nextAction) {
		this.nextAction = nextAction;
	}
	
	public void dictateYesOrNo() {
		this.status = EventStatus.LISTENING;
		List<ResultText> result = mEngine.dictateText(this, Constants.LISTEN_YES_NO_TIME_OUT);
		
		if(status != EventStatus.DONE_OK) {
			nextAction = NextAction.TRYAGAIN;
			return;
		}
		
		String text;
		for(ResultText t: result) {
			text = t.getText().toUpperCase();
			if(mResourceManager.wordPositive(text)) {
				nextAction = NextAction.YES;
				break;
			}
			if(mResourceManager.wordNegative(text)) {
				nextAction = NextAction.NO;
				break;
			}
			
			if(mResourceManager.wordStop(text)) {
				nextAction = NextAction.ABORT;
				break;
			}
		}
	}
	
	
	public abstract void dectateContent();
	
	public abstract void tryAgain();

	/**
	 * If app set auto read the notification, we have to
	 * farther check this one.
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
	
	public abstract void reply();
	
	public static final Event createEvent(StatusBarNotification sbn) {
		String name = ResourceManager.getInstance().getAppName(sbn.getPackageName());
		boolean autoRead = AppSettings.getInstance().isAutoRead();
		Context c = App.getStaticContext();
		Event event = new NotificationEvent(c);
		String question = null;
		if(autoRead) {
			question = c.getString(R.string.new_notification_auto_read, name);
		}else {
			question = c.getString(R.string.new_notification_none_auto_read, name);
		}
		l.debug("create NotificationEvent question to ask:" + question);
		event.setQuestionToAsk(question);
		String title = NotificationUtils.getTitle(sbn.getNotification());
		String content = NotificationUtils.getContent(sbn.getNotification());
		event.setTitle(c.getString(R.string.new_notification_title,title));
		event.setContent(c.getString(R.string.new_notification_content, content));
		return event;
	}
}
