package com.irefire.android.imdriving;

import android.app.Application;
import android.content.res.Configuration;

import com.irefire.android.imdriving.utils.NaunceInfo;
import com.nuance.nmdp.speechkit.SpeechKit;

public class App extends Application {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		sSpeechKit = SpeechKit.initialize(this, NaunceInfo.SpeechKitAppId,
				NaunceInfo.SpeechKitServer, NaunceInfo.SpeechKitPort,
				NaunceInfo.SpeechKitSsl, NaunceInfo.SpeechKitApplicationKey);
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	private static SpeechKit sSpeechKit;
	
	public static SpeechKit getSpeechKit() {
		assert sSpeechKit != null;
		return sSpeechKit;
	}
}
