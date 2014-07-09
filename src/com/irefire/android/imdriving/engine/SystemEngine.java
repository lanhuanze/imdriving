package com.irefire.android.imdriving.engine;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import com.irefire.android.imdriving.event.Event;

public class SystemEngine implements Engine {
	
	public static boolean isSystemEngineSupported(Context c) {
		assert c!= null;
		PackageManager pm = c.getPackageManager();
		//pm.get
		return true;
	}
	
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
	private TextToSpeech     mTextToSpeech;

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

	private static final class RecognizerListener implements RecognitionListener {
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
