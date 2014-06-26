package com.irefire.android.imdriving.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.irefire.android.imdriving.event.Event;
import com.irefire.android.imdriving.utils.AppSettings;
import com.irefire.android.imdriving.utils.Constants;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.Vocalizer;

public final class NotifcationProcessor {

	private static final class Holder {
		public static final NotifcationProcessor _INST = new NotifcationProcessor();
	}

	public static final NotifcationProcessor getInstance() {
		return Holder._INST;
	}

	private Thread processThread = null;

	private boolean stopProcessThread = false;

	private BlockingQueue<Event> events = new LinkedBlockingQueue<Event>();

	private NotifcationProcessor() {

	}

	public void start() {
		if (processThread == null) {
			processThread = new EventProcessThread();
		}
		processThread.start();
	}

	public void stop() {
		stopProcessThread = true;
		processThread = null;
	}

	public boolean enqueueEvent(Event e) {
		return events.offer(e);
	}

	private class EventProcessThread extends Thread {
		private AppSettings settings = AppSettings.getInstance();
		private StringBuilder builder = new StringBuilder(128);

		public void run() {
			while (!stopProcessThread) {
				
				/**
				 * TODO: If the audio system is busy, we should wait here.
				 */
				
				try {
					Event e = events.take();
					speak(e.getTips(), e); // speak the tips.

					// we should wait for speak finish.
					synchronized (e) {
						e.wait(Constants.SPEAK_TIME_OUT);
					}

					if (settings.isAutoRead() && e.autoActionable()) {
						e.positiveAction();
					} else {
						speak(e.getQuestionToAsk(), e);

					}
				} catch (InterruptedException e) {

				}
			}
			events.clear();
		}
	}

	private void speak(String text, Event e) {

		try {
			// we should wait for speak finish.
			synchronized (e) {
				e.wait(Constants.SPEAK_TIME_OUT);
			}
		} catch (InterruptedException ex) {

		}
	}
}
