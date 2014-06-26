package com.irefire.android.imdriving.event;

import android.content.Context;

public class NotificationEvent extends Event {

	public NotificationEvent(Context c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void positiveAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void negativeAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean autoActionable() {
		/**
		 * Notification event can be automatically read.
		 */
		return true;
	}

}
