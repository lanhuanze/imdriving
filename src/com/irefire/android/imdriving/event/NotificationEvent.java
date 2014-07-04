package com.irefire.android.imdriving.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

public class NotificationEvent extends Event {
	
	private static final Logger l = LoggerFactory.getLogger(NotificationEvent.class);
	
	public NotificationEvent(Context c) {
		super(c);
	}

	@Override
	public void positiveAction() {
		l.debug("speak title:" + title);
		mEngine.speak(title, this);
		
		// Sleep 0.5 to make sound better.
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			l.warn("positiveAction sleep exception:" + e);
		}
		
		l.debug("speak content:" + content);
		mEngine.speak(content, this);
	}

	@Override
	public void negativeAction() {
		l.debug("Negative action do nothing.");
	}

	@Override
	public boolean autoActionable() {
		/**
		 * Notification event can be automatically read.
		 */
		return true;
	}

	@Override
	public void dectateContent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tryAgain() {
	}

	@Override
	public void reply() {
		// TODO Auto-generated method stub
		
	}

}
