package com.irefire.android.imdriving.event;

import java.util.Collections;
import java.util.List;

import android.content.Context;

import com.irefire.android.imdriving.service.ResultText;

/**
 * This class is abstract class of the event.
 * @author Huanze.Lan
 *
 */
public abstract class Event {
	
	public static enum EventStatus {
		DONE_OK, DONE_ERROR,INPROGRESSING;
	}
	
	protected Context mContext = null;
	
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
	protected EventStatus status;
	
	protected List<ResultText> mRecognitionResult;
	
	public Event(Context c) {
		mContext = c;
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
}
