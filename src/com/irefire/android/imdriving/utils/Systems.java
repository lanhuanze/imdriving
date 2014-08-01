package com.irefire.android.imdriving.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
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

    public boolean isMessageApp(String pkg, Context context) {
        // we will check if an app is message app according to
        // http://android-developers.blogspot.com/2013/10/getting-your-sms-apps-ready-for-kitkat.html
        PackageManager pm = context.getPackageManager();
        boolean hasHeadlessSmsSendService = false;
        boolean hasSmsReceiver = false;
        boolean hasMmsReceive = false;
        boolean hasComposeSmsActivity = false;

        Intent headlessSmsSendServiceIntent = new Intent();
        headlessSmsSendServiceIntent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
        headlessSmsSendServiceIntent.addCategory("android.intent.category.DEFAULT");
        headlessSmsSendServiceIntent.


        return true;
    }

    public static List<String> getMessagePackages() {

        return null;
    }
}
