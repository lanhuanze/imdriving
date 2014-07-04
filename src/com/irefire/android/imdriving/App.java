package com.irefire.android.imdriving;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.irefire.android.imdriving.utils.NaunceInfo;
import com.nuance.nmdp.speechkit.Prompt;
import com.nuance.nmdp.speechkit.SpeechKit;

public class App extends Application {

	@Override
	public void onCreate() {
		sContext = this;
		super.onCreate();
		sSpeechKit = SpeechKit.initialize(this, NaunceInfo.SpeechKitAppId,
				NaunceInfo.SpeechKitServer, NaunceInfo.SpeechKitPort,
				NaunceInfo.SpeechKitSsl, NaunceInfo.SpeechKitApplicationKey);
		new Thread() {
			public void run() {
				sSpeechKit.connect();
				Prompt beep = sSpeechKit.defineAudioPrompt(R.raw.beep);
				sSpeechKit.setDefaultRecognizerPrompts(beep, Prompt.vibration(100), null, null);
			}
		}.start();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
		sContext = null;
		sSpeechKit.release();
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
	
	private static Context sContext;
	
	public static Context getStaticContext() {
		assert sContext != null;
		return sContext;
	}
}
