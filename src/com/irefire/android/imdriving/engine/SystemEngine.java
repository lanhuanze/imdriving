package com.irefire.android.imdriving.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.irefire.android.imdriving.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.irefire.android.imdriving.event.Event;

public class SystemEngine implements Engine {

	private static final Logger l = LoggerFactory.getLogger(SystemEngine.class);

	public static boolean isSystemEngineSupported(Context c) {
		assert c != null;
		PackageManager pm = c.getPackageManager();
		boolean enabledPackage = false;
		List<ResolveInfo> services = pm.queryIntentServices(new Intent(RecognitionService.SERVICE_INTERFACE), 0);
		if (services != null && services.size() > 0) {
			for (ResolveInfo ri : services) {
				boolean enabled = isPackageEnabled(ri.serviceInfo.packageName, pm);
				l.debug("REC package:" + ri.serviceInfo.packageName + " enable status = " + enabled);
				if (!enabledPackage) {
					enabledPackage = enabled;
				}
			}

			// 如果没有enabled的package.
			if (!enabledPackage) {
				return false;
			}
		} else {
			l.warn("No recognition service.");
			return false;
		}

		enabledPackage = false;
		services = pm.queryIntentServices(new Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE), 0);

		if (services != null && services.size() > 0) {
			for (ResolveInfo ri : services) {
				boolean enabled = isPackageEnabled(ri.serviceInfo.packageName, pm);
				l.debug("TTS package:" + ri.serviceInfo.packageName + " enable status = " + enabled);
				if (!enabledPackage) {
					enabledPackage = enabled;
				}
			}
			return enabledPackage;
		} else {
			l.warn("No tts service.");
			return false;
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
		return Holder._INST;
	}

    private static final class Holder {
        public static final SystemEngine _INST = new SystemEngine();
    }

    private SystemEngine() {
        init(App.getStaticContext());
    }

	private boolean initialized = false;

	public synchronized void init(Context c) {
        if(initialized) {
            return;
        }
		assert c != null;
		mContext = c;
		PackageManager pm = c.getPackageManager();

		List<ResolveInfo> services = pm.queryIntentServices(new Intent(RecognitionService.SERVICE_INTERFACE), 0);
		ServiceInfo serviceInfo = null;
		if (services != null) {
			for (ResolveInfo ri : services) {
				if (isPackageEnabled(ri.serviceInfo.packageName, pm)) {
					serviceInfo = ri.serviceInfo;
					if (ri.serviceInfo.packageName.startsWith("com.google")) {
						// 优先选用google的引擎
						break;
					}
				}
			}

			if (serviceInfo == null) {
				serviceInfo = services.get(0).serviceInfo;
			}
		}

		assert serviceInfo != null;

		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(c, new ComponentName(serviceInfo.packageName,
				serviceInfo.applicationInfo.className));

		services = pm.queryIntentServices(new Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE), 0);
		serviceInfo = null;
		if (services != null) {
			for (ResolveInfo ri : services) {
				if (isPackageEnabled(ri.serviceInfo.packageName, pm)) {
					serviceInfo = ri.serviceInfo;
					if (ri.serviceInfo.packageName.startsWith("com.google")) {
						break;
					}
				}
			}
		}
		if (serviceInfo != null) {
			mTextToSpeech = new TextToSpeech(mContext, mTextToSpeechInitListener, serviceInfo.packageName);
		} else {
			mTextToSpeech = new TextToSpeech(mContext, mTextToSpeechInitListener);
		}
		mTextToSpeech.setOnUtteranceProgressListener(mSpeakListener);
		initialized = true;
	}

	/**
	 * 语音识别引擎
	 */
	private SpeechRecognizer mSpeechRecognizer;
	private TextToSpeech mTextToSpeech;

	private Context mContext;

	private SpeakListener mSpeakListener = new SpeakListener();

	@Override
	public synchronized SpeakResult speak(String text, Event e) {
		assert initialized;
		SpeakResult result = new SpeakResult();
		result.utteranceId = String.valueOf(System.currentTimeMillis());
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, result.utteranceId);
		mSpeakListener.add(result);
		int retValue = mTextToSpeech.speak(text, TextToSpeech.QUEUE_ADD, params);
		l.debug("speak text:" + text + ", returns " + retValue);
		if (TextToSpeech.SUCCESS == retValue) {
			// 我们要等到说完了才返回,通知在SpeakListener里。
			try {
				synchronized (result) {
					result.wait();
				}
			} catch (InterruptedException e1) {
				l.warn("InterruptedException occurred when waiting for result.");
			}
		}else {
			result.result = Engine.EngineResult.FAILED;
		}
		return result;
	}

	private static boolean isPackageEnabled(String packageName, PackageManager pm) {

		ApplicationInfo ai = null;
		try {
			ai = pm.getApplicationInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			l.warn("package " + packageName + " not found with exception:" + e);
		}
		return ai != null && ai.enabled;
	}

	@Override
	public synchronized DictationResult dictateText(Event e, long timeout) {
		DictationResult result = new DictationResult();
		RecognizerListener listener = new RecognizerListener(result);
		assert initialized;
		Intent recognizerIntent = new Intent();
		mSpeechRecognizer.setRecognitionListener(listener);
		mSpeechRecognizer.startListening(recognizerIntent);
		return result;
	}

	private static final class RecognizerListener implements RecognitionListener {
		private DictationResult target = null;

		public RecognizerListener(DictationResult result) {
			this.target = result;
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

	private static final class SpeakListener extends UtteranceProgressListener {
		private Map<String, SpeakResult> targets;

		public SpeakListener() {
			targets = new HashMap<String, SpeakResult>();
		}

		@Override
		public void onStart(String utteranceId) {
			SpeakResult result = targets.get(utteranceId);
			if(result == null) {
				l.warn("Get result of " + utteranceId + " is empty.");
				return;
			}
			result.speakStartTime = System.currentTimeMillis();
		}

		@Override
		public void onDone(String utteranceId) {
			SpeakResult result = targets.get(utteranceId);
			if(result == null) {
				l.warn("Get result of " + utteranceId + " is empty.");
				return;
			}
			result.speakEndTime = System.currentTimeMillis();
			
			synchronized (targets) {
				l.debug("remove result of " + utteranceId);
				targets.remove(utteranceId);
			}
			
			synchronized(result) {
				result.notifyAll();
			}
		}

		@Override
		public void onError(String utteranceId) {
			SpeakResult result = targets.get(utteranceId);
			if(result == null) {
				l.warn("Get result of " + utteranceId + " is empty.");
				return;
			}
			result.speakEndTime = System.currentTimeMillis();
			result.result = Engine.EngineResult.FAILED;
			
			synchronized (targets) {
				l.debug("remove result of " + utteranceId);
				targets.remove(utteranceId);
			}
			
			synchronized(result) {
				result.notifyAll();
			}

		}

		public void add(SpeakResult sr) {
			if (sr == null) {
				l.warn("Try to add a null SpeakResult");
				return;
			}
			synchronized (targets) {
				targets.put(sr.utteranceId, sr);
			}
		}

	}

	private TextToSpeech.OnInitListener mTextToSpeechInitListener = new TextToSpeech.OnInitListener() {

		@Override
		public void onInit(int status) {
			l.debug("TextToSpeech.OnInitListener status = " + status);
		}
	};
}
