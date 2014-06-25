package com.irefire.android.imdriving.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Systems {
	private static final Logger l = LoggerFactory.getLogger(Systems.class);
	public static final String getSystemPath() {
		StringBuilder builder = new StringBuilder(256);
		BufferedReader reader = null;
		try {
			Process p = Runtime.getRuntime().exec("sh echo $PATH");
			p.waitFor();
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (IOException e) {
			l.warn("getSystemPath get IOException:" + e.getMessage());
		} catch (InterruptedException e) {
			l.warn("getSystemPath get InterruptedException:" + e.getMessage());
		}finally {
			SafeIO.close(reader);
		}
		
		l.debug("getSystemPath returns " + builder.toString());
		return builder.toString();
	}
}
