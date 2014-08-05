package com.irefire.android.imdriving.event;

import com.irefire.android.imdriving.R;
import com.irefire.android.imdriving.engine.SpeakResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

import com.irefire.android.imdriving.engine.Engine.EngineResult;

public class NotificationEvent extends Event {
	
	private static final Logger l = LoggerFactory.getLogger(NotificationEvent.class.getSimpleName());
	
	public NotificationEvent(Context c) {
		super(c);
	}

	@Override
	public boolean actionReadNotification() {
		String readText = mResourceManager.getString(R.string.new_notification_title_and_content, title, content);
		SpeakResult result = mEngine.speak(readText, this);
		l.debug("Speak:" + readText +", returns " + result);
		this.setNextAction(NextAction.ACTION_DONE);
        return true;
	}

	@Override
	public boolean actionIgnoreNotification() {
		l.debug("Negative action do nothing.");
		this.setNextAction(NextAction.ACTION_DONE);
        return true;
	}

	@Override
	public boolean autoActionable() {
		/**
		 * Notification event can be automatically read.
		 */
		return true;
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
}
