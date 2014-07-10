package com.irefire.android.imdriving.engine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.irefire.android.imdriving.event.Event;

public class SystemEngine implements Engine {

	private static final Logger l = LoggerFactory.getLogger(SystemEngine.class);

	public static boolean isSystemEngineSupported(Context c) {
		assert c != null;
		PackageManager pm = c.getPackageManager();

		List<ResolveInfo> services = pm.queryIntentServices(new Intent(
				RecognitionService.SERVICE_INTERFACE), 0);
		if (services == null || services.size() == 0) {
			l.debug("No recognition service.");
			return false;
		}
		
		services = pm.queryIntentServices(new Intent(
				RecognitionService.SERVICE_INTERFACE), 0);
		
		ComponentName reconitionComponent = null;
		for (ResolveInfo ri : services) {
			l.debug("ResolveInfo name:" + ri.serviceInfo.name);
			l.debug("ResolveInfo package:" + ri.serviceInfo.packageName);
			l.debug("ResolveInfo process:" + ri.serviceInfo.processName);
			if (ri.serviceInfo.packageName.startsWith("com.google")) {
				reconitionComponent = new ComponentName(
						ri.serviceInfo.packageName, ri.serviceInfo.name);
			}
		}
	}

	/*
	 * final SpeechRecognizer speechRecognizer =
	 * SpeechRecognizer.createSpeechRecognizer(this, reconitionComponent);
	 * Log.d("XXXXX", "supported language:" + speechRecognizer);
	 * 
	 * speechRecognizer.setRecognitionListener(mRecognitionListener); Intent
	 * recognizerIntent = new Intent();
	 * recognizerIntent.setAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	 * recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh_CN");
	 * speechRecognizer.startListening(recognizerIntent);
	 * 
	 * mHandler.postDelayed(new Runnable() { public void run() {
	 * speechRecognizer.stopListening(); } }, 3000); return true; }
	 */
	public static final SystemEngine getInstance() {
		return null;
	}

	private boolean initialized = false;

	public synchronized void init(Context c) {
		assert c != null;

		initialized = true;
	}

	/**
	 * 语音识别引擎
	 */
	private SpeechRecognizer mSpeechRecognizer;
	private TextToSpeech mTextToSpeech;

	@Override
	public synchronized EngineResult speak(String text, Event e) {
		assert initialized;
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public synchronized DictationResult dictateText(Event e, long timeout) {
		DictationResult result = new DictationResult();
		RecognitionListener listener = new RecognizerListener(result);
		assert initialized;
		Intent recognizerIntent = new Intent();
		mSpeechRecognizer.setRecognitionListener(listener);
		mSpeechRecognizer.startListening(recognizerIntent);
		return null;
	}

	private static final class RecognizerListener implements
			RecognitionListener {
		private DictationResult target = null;

		public RecognizerListener(DictationResult t) {
			this.target = e;
		}

		@Override
		public void onReadyForSpeech(Bundle params) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onBeginningOfSpeech() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRmsChanged(float rmsdB) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onBufferReceived(byte[] buffer) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onEndOfSpeech() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(int error) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onResults(Bundle results) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPartialResults(Bundle partialResults) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onEvent(int eventType, Bundle params) {
			// TODO Auto-generated method stub

		}
	}
}
