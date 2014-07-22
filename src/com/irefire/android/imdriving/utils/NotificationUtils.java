package com.irefire.android.imdriving.utils;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.os.Build;
import android.os.Bundle;

public class NotificationUtils {
	private static final Logger l = LoggerFactory
			.getLogger(NotificationUtils.class.getSimpleName());

	@SuppressLint("NewApi")
	public static final String getTitle(Notification n) {
		String title = "";
		Bundle b = null;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
			b = getBundle(n);
			if(b != null) {
				title = b.getCharSequence("android.title").toString();
			}
		}else {
			b = n.extras;
			if(b != null) {
				title = b.getCharSequence(Notification.EXTRA_TITLE).toString();
			}
		}
		return title;
	}

	@SuppressLint("NewApi")
	public static final String getContent(Notification n) {
		String content = "";
		Bundle b = null;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
			b = getBundle(n);
			if(b != null) {
				content = b.getCharSequence("android.text").toString();
			}
		}else {
			b = n.extras;
			if(b != null) {
				content = b.getCharSequence(Notification.EXTRA_TEXT).toString();
			}
		}
		return content;
	}

	private final static Bundle getBundle(Notification n) {
		try {
			Field f = Notification.class.getField("extras");
			if (f != null) {
				Bundle b = (Bundle) f.get(n);
				return b;
			}
		} catch (Exception e) {
			l.warn("try to get notification field extras failed: " + e);
		}
		return null;
	}
}
