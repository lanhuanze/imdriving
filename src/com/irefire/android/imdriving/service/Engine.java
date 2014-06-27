package com.irefire.android.imdriving.service;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.text.TextUtils;

import com.irefire.android.imdriving.App;
import com.irefire.android.imdriving.event.Event;
import com.irefire.android.imdriving.event.Event.EventStatus;
import com.irefire.android.imdriving.utils.AppSettings;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;
import com.nuance.nmdp.speechkit.Vocalizer;

public class Engine implements AppSettings.SettingChangeListener{
	private static final Logger l = LoggerFactory.getLogger(Engine.class);

	private static final class Holder {
		public static final Engine _INST = new Engine();
	}

	public static final Engine getInstance() {
		return Holder._INST;
	}

	private Engine() {
		mSettings = AppSettings.getInstance();
		
		//we will get notification when settings changed.
		mSettings.addListener(this);
		
		mSpeechKit = App.getSpeechKit();
		
		createVocalizerAndRecognizer();
		
		mCheckBitSet = new BitSet();
		mCheckBitSet.set(AppSettings.SettingItem.LANGUAGE.ordinal());
		mCheckBitSet.set(AppSettings.SettingItem.VOICE.ordinal());
	}

	private SpeechKit mSpeechKit;
	private Vocalizer mVocalizer;
	private Recognizer mRecognizer;
	private AppSettings mSettings;
	private BitSet mCheckBitSet;

	/**
	 * Synchronized function to speak a text. This function will return after
	 * speak end.
	 * 
	 * @param text
	 * @param target
	 * @return
	 */
	public boolean speak(String text, Event target) {
		l.debug("speak text:" + text);
		if(TextUtils.isEmpty(text)) {
			l.warn("We won't speak empty text");
			return false;
		}
		// We asign the old mVocalizer to a local variable in case when
		// update it when settings change.
		Vocalizer vocalizer = null;
		synchronized(mSpeechKit) {
			vocalizer = mVocalizer;
		}
		vocalizer.speakString(text, target);
		l.debug("speak text:" + text + ", begin to wait speak.");
		// we will wait until speak finish.
		try {
			if (target != null) {
				synchronized (target) {
					target.wait();
					l.debug("speak text:" + text + ", got notified..");
				}
			}
		} catch (InterruptedException e) {
			l.debug("speak text:" + text + " got exception:" + e);
		}
		return target.getEventStatus() == EventStatus.DONE_OK;
	}

	/**
	 * 
	 */

	public List<ResultText> dictateText(Event target) {
		l.debug("dictateText start dictate text");
		mDictationListener.setTargetObject(target);
		
		// We asign the old mRecognizer to a local variable in case when
		// update it when settings change.
		Recognizer recognizer = null;
		synchronized(mSpeechKit) {
			recognizer = mRecognizer;
		}
		recognizer.setListener(mDictationListener);
		recognizer.start();
		l.debug("dictateText  waiting");
		try {
			if (target != null) {
				synchronized (target) {
					target.wait();
					l.debug("dictateText got notified..");
				}
			}
		} catch (InterruptedException e) {
			l.debug("dictateText got exception:" + e);
		}
		if(target != null) {
			return target.getRecognitionResult();
		}
		return Collections.emptyList();
	}

	private void setError(Object target, SpeechError error) {
		if (target instanceof Event) {
			((Event) target).setEventStatus(error == null ? EventStatus.DONE_OK
					: EventStatus.DONE_ERROR);
		}
	}

	private List<ResultText> setResult(Object target, Recognition result) {
		List<ResultText> results = new ArrayList<ResultText>();
		
		if(result != null) {
			int count = result.getResultCount();
			l.debug("Recognition has " + count +" result, suggestion:" + result.getSuggestion());
			Recognition.Result r = null;
			for(int i = 0; i < count; i++) {
				r = result.getResult(i);
				l.debug("Recognition result[ "+ i +"], score:" + r.getScore() +", text:" + r.getText());
				results.add(new ResultText(r.getScore(), r.getText()));
			}
		}
		
		if (target instanceof Event) {
			((Event) target).setSuggestion(result.getSuggestion());
			((Event) target).setRecognitionResult(results);
		}
		return results;
	}
	
	private void createVocalizerAndRecognizer() {
		mVocalizer = mSpeechKit.createVocalizerWithLanguage(
				mSettings.getTtsLanguage(), mSpeakListener, null);
		mVocalizer.setVoice(mSettings.getTtsVoice());

		mRecognizer = mSpeechKit.createRecognizer(
				Recognizer.RecognizerType.Dictation,
				Recognizer.EndOfSpeechDetection.Long,
				mSettings.getTtsLanguage(), mDictationListener, null);
		l.debug("Engine inited with language:" + mSettings.getTtsLanguage()
				+ ", voice:" + mSettings.getTtsVoice());
	}

	private DictationListener mDictationListener = new DictationListener();

	private class DictationListener implements Recognizer.Listener {

		private Object target = null;

		public void setTargetObject(Object o) {
			target = o;
		}

		@Override
		public void onError(Recognizer recognizer, SpeechError error) {
			l.debug("DictationListener.onError:" + error);
			if (target != null) {
				setError(target, error);
				synchronized (target) {
					target.notify();
				}
			}
		}

		@Override
		public void onRecordingBegin(Recognizer recognizer) {
			l.debug("DictationListener.onRecordingBegin");
		}

		@Override
		public void onRecordingDone(Recognizer recognizer) {
			l.debug("DictationListener.onRecordingDone");
		}

		@Override
		public void onResults(Recognizer recognizer, Recognition result) {
			l.debug("DictationListener.onResults");
			if (target != null) {
				setError(target, null);
				setResult(target, result);
				synchronized (target) {
					target.notify();
				}
			}
		}

	};
	
	private Vocalizer.Listener mSpeakListener = new Vocalizer.Listener() {

		@Override
		public void onSpeakingBegin(Vocalizer vocalizer, String text,
				Object target) {
			l.debug("Begin speak:" + text);
		}

		@Override
		public void onSpeakingDone(Vocalizer vocalizer, String text,
				SpeechError error, Object target) {
			l.debug("Done speak:" + text + " with error:" + error);
			setError(target, error);
			if (target != null) {
				synchronized (target) {
					target.notify();
					l.debug("notify target.");
				}
			}
		}

	};

	@Override
	public void onChange(AppSettings settings, BitSet set) {
		if(set.intersects(mCheckBitSet)) {
			//TODO, we will update our mVocalizer and mRecognizer here.
			synchronized(mSpeechKit) {
				createVocalizerAndRecognizer();
			}
			l.debug("Recreate createVocalizerAndRecognizer by settings changed.");
		}
	}

}
