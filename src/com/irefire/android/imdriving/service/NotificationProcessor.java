package com.irefire.android.imdriving.service;

import com.irefire.android.imdriving.event.Event;
import com.irefire.android.imdriving.event.Event.NextAction;
import com.irefire.android.imdriving.event.EventSource;
import com.irefire.android.imdriving.utils.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class NotificationProcessor {

    private static final Logger l = LoggerFactory
            .getLogger(NotificationProcessor.class.getSimpleName());
    private Thread processThread = null;
    private Thread generateThread = null;
    private boolean stopProcessThread = false;
    private boolean processThreadStarted = false;
    private boolean stopGenerateThread = false;
    private boolean generateThreadStarted = false;
    private AppSettings mSettings = null;
    private BlockingQueue<Event> events = new LinkedBlockingQueue<Event>();
    private BlockingQueue<EventSource> eventsources = new LinkedBlockingQueue<EventSource>();

    private NotificationProcessor() {
        mSettings = AppSettings.getInstance();
    }

    public static final NotificationProcessor getInstance() {
        return Holder._INST;
    }

    public void start() {
        if (!processThreadStarted) {
            if (processThread == null) {
                processThread = new EventProcessThread();
            }
            processThread.start();
            processThreadStarted = true;
        }

        if (!generateThreadStarted) {
            if (generateThread == null) {
                generateThread = new EventGenerateThread();
            }
            generateThread.start();
            generateThreadStarted = true;
        }
    }

    public void stop() {
        stopProcessThread = true;
        stopGenerateThread = true;
        if (processThread != null) {
            processThread.interrupt();
        }
        processThread = null;

        if (generateThread != null) {
            generateThread.interrupt();
        }
        generateThread = null;
//        eventsources.clear();
//        events.clear();

    }

    public boolean enqueueEventSource(EventSource es) {
        if (mSettings.getServiceStarted() && es != null && es.valid()) {
            return eventsources.offer(es);
        }
        return false;
    }

    private static final class Holder {
        public static final NotificationProcessor _INST = new NotificationProcessor();
    }

    private class EventGenerateThread extends Thread {
        public void run() {
            try {
                while (!stopGenerateThread) {
                    try {
                        EventSource es = eventsources.take();
                        if(es.notification != null) {
                            events.offer(Event.createEvent(es.notification));
                        }else if(es.sms != null) {
                            events.offer(Event.createEvent(es.sms));
                        }
                    } catch (InterruptedException e) {
                        l.warn("Get exception:" + e);
                    }
                }
                eventsources.clear();
            } finally {
                generateThreadStarted = false;
            }
        }
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
                        //StatusBarNotification sbn = events.take();
                        //Event e = Event.createEvent(sbn);
                        Event e = events.take();
                        while (e.getNextAction() != NextAction.ACTION_DONE) {
                            switch (e.getNextAction()) {
                                /** 播报新消息来到 */
                                case SPEAK_ARRIVING_TIP:
                                    e.speakArrivingTip();
                                    break;
                                /** 询问用户是否读新消息, 如果是来电，则问是否接听 */
                                case ASK_IF_READ_NOTIFICATION:
                                    e.speakAskIfReadMessage();
                                    break;
                                /** 如果听取失败，则让用户再说一次*/
                                case ASK_IF_READ_NOTIFICATION_AGAIN:
                                    e.speakAskIfReadMessageAgain();
                                    break;
                                /** 听取是否读消息或是接听电话 */
                                case ACTION_DICTATE_IF_READ_NOTIFICATION:
                                    e.dictateIfReadMessage();
                                    break;
                                /** 用户选择读取消息或是接听电话 */
                                case ACTION_POSITIVE:
                                    e.positiveAction();
                                    break;
                                /** 用户选择不读消息或不接听电话 */
                                case ACTION_NEGATIVE:
                                    e.negativeAction();
                                    break;
                                /** 询问用户是否回复信息，（电话只有拒接了才询问，短信只有读了才回复) */
                                case SPEAK_ASK_IF_REPLY:
                                    e.speakAskIfReply();
                                    break;
                                /** 告诉用户无法回复，比如我们的程序没有设置为默认的短信程序 */
                                case SPEAK_UNABLE_TO_REPLY:
                                    e.speakUnableToReply();
                                    break;
                                /** 提示用户开始听用户的回复的内容 */
                                case SPEAK_DICTATE_REPLY_CONTENT_START:
                                    e.speakDictateReplyContentStart();
                                    break;
                                /** 提示用户没有听到任何内容，然后询问是否重说一遍 */
                                case SPEAK_DICTATE_REPLY_CONTENT_FAILED:
                                    e.speakDictateReplyContentFailed();
                                    break;
                                /** 提示用户上次听取失败，重新开始听用户的回复的内容 */
                                case SPEAK_DICTATE_REPLY_CONTENT_START_AGAIN:
                                    e.speakDictateReplyContentStartAgain();
                                    break;
                                /** 重复一遍用户输入的内容 */
                                case SPEAK_REPEAT_REPLY_CONTENT:
                                    e.speakRepeatReplyContent();
                                    break;
                                /** 询问用户是否确定回复 */
                                case ASK_IF_SENT_REPLY:
                                    e.speakAskIfSentReply();
                                    break;
                                /** 上次听取失败，再次询问用户是否确定回复 */
                                case ASK_IF_SENT_REPLY_AGAIN:
                                    e.speakAskIfSentReplyAgain();
                                    break;
                                /** 开始回复 */
                                case ACTION_REPLY:
                                    e.reply();
                                    break;
                                /** 发送成功 */
                                case SPEAK_REPLY_OK:
                                    e.speakReplyOk();
                                    break;
                                /** 发送失败 */
                                case SPEAK_REPLY_FAILED:
                                    e.speakReplyFailed();
                                    break;
                                /** 程序即将退出 */
                                case SPEAK_ABOAT_ABORTING:
                                    e.speakAboutAborting();
                                    break;
                                /** 失败次数太多 */
                                case SPEAK_TOO_MANY_FAILED_TRIALS:
                                    e.speakTooManyFailedTrials();
                                    break;
                                default:
                                    l.error("Unknown next action:" + e.getNextAction());
                                    break;
                            }
                        }
                    } catch (InterruptedException e) {
                        l.warn("Get exception:" + e);
                    }
                }
                events.clear();

            } finally {
                processThreadStarted = false;
            }
        }
    }
}
