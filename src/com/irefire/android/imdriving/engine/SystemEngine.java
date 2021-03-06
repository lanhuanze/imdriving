package com.irefire.android.imdriving.engine;

import java.util.*;

import android.os.Handler;
import android.speech.RecognizerIntent;
import com.irefire.android.imdriving.App;
import com.irefire.android.imdriving.event.DummyEvent;
import com.irefire.android.imdriving.utils.AppSettings;
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

public class SystemEngine implements Engine, AppSettings.SettingChangeListener {

	private static final Logger l = LoggerFactory.getLogger(SystemEngine.class.getSimpleName());

    private BitSet mCheckBitSet = new BitSet();
    private AppSettings mSettings = null;

    @Override
    public SpeakResult speak(String text) {
        return speak(text, new DummyEvent(mContext));
    }

    public static boolean isSystemEngineSupported(Context c) {
		assert c != null;
        l.debug("isRecognitionAvailable = " + SpeechRecognizer.isRecognitionAvailable(c));
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

	public static final SystemEngine getInstance() {
		return Holder._INST;
	}

    @Override
    public void onChange(AppSettings settings, BitSet set) {
        if(set.intersects(mCheckBitSet)) {
            mTextToSpeech.setLanguage(new Locale(mSettings.getTtsLanguage()));
        }
    }

    private static final class Holder {
        public static final SystemEngine _INST = new SystemEngine();
    }

    private SystemEngine() {
        init(App.getStaticContext());
        mCheckBitSet.set(AppSettings.SettingItem.LANGUAGE.ordinal());
        mSettings = AppSettings.getInstance();
        mSettings.addListener(this);
    }

	private boolean initialized = false;

	public synchronized void init(Context c) {
        if(initialized) {
            return;
        }
		assert c != null;
		mContext = c;

        mMainThreadHandler = new Handler(mContext.getMainLooper());

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
        l.debug("SpeechRecognizer(" + serviceInfo.packageName +"/" + serviceInfo.name +")");
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(c, new ComponentName(serviceInfo.packageName,
				serviceInfo.name));
        //mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(c);

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
       // mTextToSpeech.setLanguage(new Locale(mSettings.getTtsLanguage()));
		mTextToSpeech.setOnUtteranceProgressListener(mSpeakListener);
		initialized = true;
	}

	/**
	 * 语音识别引擎
	 */
	private SpeechRecognizer mSpeechRecognizer;
	private TextToSpeech mTextToSpeech;

	private Context mContext;

    private Handler mMainThreadHandler = null;

	private SpeakListener mSpeakListener = new SpeakListener();

	@Override
	public synchronized SpeakResult speak(String text, Event e) {
		assert initialized;
		SpeakResult result = new SpeakResult();
		result.utteranceId = String.valueOf(System.currentTimeMillis());
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, result.utteranceId);
		mSpeakListener.add(result);
        mTextToSpeech.setLanguage(new Locale(mSettings.getTtsLanguage()));
        int retValue = mTextToSpeech.speak(text, TextToSpeech.QUEUE_ADD, params);
		l.debug("speak text:" + text + ", returns " + retValue);
		if (TextToSpeech.SUCCESS == retValue) {
			// 我们要等到说完了才返回,通知在SpeakListener里。
			try {
				synchronized (result) {
                    if(!result.isSpeakFinish) {
                        result.wait();
                    }
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
		final RecognizerListener listener = new RecognizerListener(result);
		assert initialized;
		final Intent recognizerIntent = new Intent();
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mSettings.getTtsLanguage());
        // we must call this from main thread.
        mMainThreadHandler.post( new Runnable() {
            @Override
            public void run() {
                mSpeechRecognizer.setRecognitionListener(listener);
                mSpeechRecognizer.startListening(recognizerIntent);
            }
        });

        if(timeout > 0) synchronized (result) {
            try {
                l.debug("waiting for speech finish.");
                result.wait(timeout);
            } catch (InterruptedException e1) {
                l.warn("result timeout wait error:" + e1);
            }
        }

        // we must call this from main thread.
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mSpeechRecognizer.stopListening();
            }
        });


        synchronized (result) {
            if(!listener.isDictateFinished()) {
                try {
                    l.debug("waiting for dictate result.");
                    result.wait();
                } catch (InterruptedException e1) {
                    l.warn("result wait dictate error:" + e1);
                }
            }
        }
        l.debug("dictateText result:" + result);
		return result;
	}

	private static final class RecognizerListener implements RecognitionListener {
		private DictationResult target = null;

        private boolean mDictateFinished = false;

        public boolean isDictateFinished() {
            return mDictateFinished;
        }

		public RecognizerListener(DictationResult result) {
			this.target = result;
		}

		@Override
		public void onReadyForSpeech(Bundle params) {
            l.debug("onReadyForSpeech params:" + params);
		}

		@Override
		public void onBeginningOfSpeech() {
			l.debug("onBeginningOfSpeech");
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			//l.debug("onRmsChanged rmsdB =" + rmsdB);
		}

		@Override
		public void onBufferReceived(byte[] buffer) {
			l.debug("onBufferReceived buffer:" + buffer);
		}

		@Override
		public void onEndOfSpeech() {
			l.debug("onEndOfSpeech");
		}

		@Override
		public void onError(int error) {
            l.debug("onError error = " + error);
			switch (error) {
                case SpeechRecognizer.ERROR_NETWORK:
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    target.result = EngineResult.NETWORK_ERROR;
                    break;
                default:
                    target.result = EngineResult.FAILED;
                    break;
            }

            // Notify that we finished the dictate.
            synchronized (target) {
                mDictateFinished = true;
                target.notifyAll();
            }
		}

		@Override
		public void onResults(Bundle results) {
            ArrayList<String> texts = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
            l.debug("onResults texts: " + texts);
            l.debug("onResults scores:" + Arrays.toString(scores));
            for(int i = 0; i < texts.size(); i++) {
                ResultText rt = new ResultText();
                rt.setText(texts.get(i));
                rt.setScore((int)(scores[i] * 100));
                target.texts.add(rt);
            }
            target.result = EngineResult.OK;

            // Notify that we finished the dictate.
            synchronized (target) {
                mDictateFinished = true;
                target.notifyAll();
            }
		}

		@Override
		public void onPartialResults(Bundle partialResults) {
			l.debug("onPartialResults results:" + partialResults);
		}

		@Override
		public void onEvent(int eventType, Bundle params) {
		    l.debug("onEvent evenType = " + eventType +", params:" + params);
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
                result.isSpeakFinish = true;
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
                result.isSpeakFinish = true;
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
