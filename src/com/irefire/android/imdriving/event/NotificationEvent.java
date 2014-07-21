package com.irefire.android.imdriving.event;

import com.irefire.android.imdriving.R;
import com.irefire.android.imdriving.engine.SpeakResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

import com.irefire.android.imdriving.engine.Engine.EngineResult;

public class NotificationEvent extends Event {
	
	private static final Logger l = LoggerFactory.getLogger(NotificationEvent.class);
	
	public NotificationEvent(Context c) {
		super(c);
	}

	@Override
	public void positiveAction() {
		String readText = mResourceManager.getString(R.string.new_notification_title_and_content, title, content);
		SpeakResult result = mEngine.speak(readText, this);
		l.debug("Speak:" + readText +", returns " + result);
		this.setNextAction(NextAction.ACTION_DONE);
	}

	@Override
	public void negativeAction() {
		l.debug("Negative action do nothing.");
		this.setNextAction(NextAction.ACTION_DONE);
	}

	@Override
	public boolean autoActionable() {
		/**
		 * Notification event can be automatically read.
		 */
		return true;
	}
	
	@Override
	public void dictateContent() {
		l.error("This method should not invoked in this class.");
	}

	@Override
	public boolean speakAskIfReadMessage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean speakAskIfReply() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean speakStartDictateContent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean speakAskIfReadMessageAgain() {
		// TODO Auto-generated method stub
		return false;
	}
}
