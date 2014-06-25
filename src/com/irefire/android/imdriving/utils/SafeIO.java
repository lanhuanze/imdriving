package com.irefire.android.imdriving.utils;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeIO {
	private static final Logger l = LoggerFactory.getLogger(SafeIO.class);
	public static final void close(Closeable c) {
		if(c != null) {
			try {
				c.close();
			}catch(Exception e) {
				l.warn("Get exception:" + e);
			}
		}
	}
}
