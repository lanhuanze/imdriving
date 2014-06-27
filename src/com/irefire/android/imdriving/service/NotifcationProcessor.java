package com.irefire.android.imdriving.service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.service.notification.StatusBarNotification;

import com.irefire.android.imdriving.App;
import com.irefire.android.imdriving.R;
import com.irefire.android.imdriving.event.Event;
import com.irefire.android.imdriving.event.Event.EventStatus;
import com.irefire.android.imdriving.utils.AppSettings;
import com.irefire.android.imdriving.utils.Constants;

public final class NotifcationProcessor {

	private static final Logger l = LoggerFactory
			.getLogger(NotifcationProcessor.class);

	private static final class Holder {
		public static final NotifcationProcessor _INST = new NotifcationProcessor();
	}

	public static final NotifcationProcessor getInstance() {
		return Holder._INST;
	}

	private Thread processThread = null;
	private Engine mEngine = null;
	private List<String> positiveWords;
	private List<String> negativeWords;

	private boolean stopProcessThread = false;
	private boolean processThreadStarted = false;

	private BlockingQueue<StatusBarNotification> events = new LinkedBlockingQueue<StatusBarNotification>();

	private NotifcationProcessor() {
		mEngine = Engine.getInstance();
		positiveWords = Arrays.asList(App.getStaticContext().getResources()
				.getStringArray(R.array.positive_words));
		negativeWords = Arrays.asList(App.getStaticContext().getResources()
				.getStringArray(R.array.negative_words));
	}

	public void start() {
		if (!processThreadStarted) {
			if (processThread == null) {
				processThread = new EventProcessThread();
			}
			processThread.start();
			processThreadStarted = true;
		}
	}

	public void stop() {
		stopProcessThread = true;
		processThread = null;
	}

	public boolean enqueueEvent(StatusBarNotification sbn) {
		return events.offer(sbn);
	}

	private class EventProcessThread extends Thread {
		private AppSettings settings = AppSettings.getInstance();

		public void run() {
			try {
				while (!stopProcessThread) {

					/**
					 * TODO: If the audio system is busy, we should wait here.
					 */
					try {
						StatusBarNotification sbn = events.take();
						Event e = Event.createEvent(sbn);
						mEngine.speak(e.getTips(), e); // speak the tips.

						// we should wait for speak finish.
						synchronized (e) {
							e.wait(Constants.SPEAK_TIME_OUT);
						}

						if (settings.isAutoRead() && e.autoActionable()) {
							e.positiveAction();
						} else {
							// Speak the choice.
							mEngine.speak(e.getQuestionToAsk(), e);
							// Get the answer from user.
							List<ResultText> result = mEngine.dictateText(e);
							NextAction nextAction = getNextAction(e, result);
							l.debug("next action:" + nextAction);
							if (nextAction == NextAction.YES) {
								e.positiveAction();
							} else if (nextAction == NextAction.NO) {
								e.negativeAction();
							} else {
								// should we loop several times.
							}
						}
					} catch (InterruptedException e) {

					}
				}
				events.clear();

			} finally {
				processThreadStarted = false;
			}
		}
	}

	private NextAction getNextAction(Event e, List<ResultText> texts) {
		if (e.getEventStatus() != EventStatus.DONE_OK) {
			return NextAction.UNKNOWN;
		}

		for (ResultText r : texts) {
			String t = r.getText().toUpperCase();
			if (positiveWords.contains(t)) {
				return NextAction.YES;
			}

			if (negativeWords.contains(t)) {
				return NextAction.NO;
			}
		}
		return NextAction.UNKNOWN;
	}

	private static enum NextAction {
		YES, NO, UNKNOWN;
	}
}
