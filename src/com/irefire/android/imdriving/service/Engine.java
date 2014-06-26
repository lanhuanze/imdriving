package com.irefire.android.imdriving.service;

import com.irefire.android.imdriving.App;
import com.irefire.android.imdriving.event.Event;
import com.irefire.android.imdriving.event.Event.EventStatus;
import com.irefire.android.imdriving.utils.AppSettings;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;
import com.nuance.nmdp.speechkit.Vocalizer;

public class Engine {
	
	private static final class Holder {
		public static final Engine _INST = new Engine();
	}

	public static final Engine getInstance() {
		return Holder._INST;
	}
	
	private Engine() {
		mSpeechKit = App.getSpeechKit();
		mVocalizer = mSpeechKit.createVocalizerWithLanguage(mSettings.getTtsLanguage(), mSpeakListener, null);
		mVocalizer.setVoice(mSettings.getTtsVoice());
		
		mRecognizer = mSpeechKit.createRecognizer(Recognizer.RecognizerType.Dictation, Recognizer.EndOfSpeechDetection.Long, mSettings.getTtsLanguage(), mDictationListener, null);
	}
	
	private SpeechKit           mSpeechKit;
	private Vocalizer           mVocalizer;
	private Recognizer          mRecognizer;
	private AppSettings         mSettings;

	private Vocalizer.Listener mSpeakListener = new Vocalizer.Listener() {

		@Override
		public void onSpeakingBegin(Vocalizer vocalizer, String text,
				Object target) {

		}

		@Override
		public void onSpeakingDone(Vocalizer vocalizer, String text,
				SpeechError error, Object target) {
			if (target != null) {
				synchronized (target) {
					target.notify();
				}
			}
		}

	};
	
	/**
	 * Synchronized function to speak a text. This function will return after
	 * speak end.
	 * @param text
	 * @param target
	 * @return
	 */
	public boolean speak(String text, Event target) {
		
		return target.getEventStatus() == EventStatus.DONE_OK;
	}
	
	
	private DictationListener mDictationListener = new DictationListener();
	
	private class DictationListener implements Recognizer.Listener {
		
		private Object notifyObject = null;
		
		public void setNofityObject(Object o) {
			notifyObject = o;
		}

		@Override
		public void onError(Recognizer recognizer, SpeechError error) {
			if(notifyObject != null) {
				synchronized(notifyObject) {
					notifyObject.notify();
				}
			}
		}

		@Override
		public void onRecordingBegin(Recognizer recognizer) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onRecordingDone(Recognizer recognizer) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onResults(Recognizer recognizer, Recognition result) {
			if(notifyObject != null) {
				synchronized(notifyObject) {
					notifyObject.notify();
				}
			}
		}
		
	};
}
