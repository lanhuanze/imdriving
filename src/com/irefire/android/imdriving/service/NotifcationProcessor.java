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
import com.irefire.android.imdriving.event.Event.NextAction;
import com.irefire.android.imdriving.utils.AppSettings;

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

	private boolean stopProcessThread = false;
	private boolean processThreadStarted = false;

	private BlockingQueue<StatusBarNotification> events = new LinkedBlockingQueue<StatusBarNotification>();

	private NotifcationProcessor() {
		mEngine = Engine.getInstance();
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
						
						// Sleep 1 to make sound better.
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
							l.warn("positiveAction sleep exception:" + ie);
						}
						
						mEngine.speak(e.getTips(), e); // speak the tips.
						// Speak the choice.
						mEngine.speak(e.getQuestionToAsk(), e);
						if (settings.isAutoRead() && e.autoActionable()) {
							
							// Sleep 1 to make sound better.
							try {
								Thread.sleep(1000);
							} catch (InterruptedException ie) {
								l.warn("positiveAction sleep exception:" + ie);
							}
							
							e.positiveAction();
						} else {
							// Get the answer from user.
							e.setNextAction(NextAction.DICTATION_YES_OR_NO);
							while(e.getNextAction() != NextAction.ABORT || e.getNextAction() != NextAction.DONE) {
								switch(e.getNextAction()) {
								case DICTATION_YES_OR_NO:
									e.dictateYesOrNo();
									break;
								case YES:
									e.positiveAction();
									break;
								case NO:
									e.negativeAction();
									break;
								case TRYAGAIN:
									e.tryAgain();
									break;
								case REPLY:
									e.reply();
									break;
								default:
									break;
								}
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
}
