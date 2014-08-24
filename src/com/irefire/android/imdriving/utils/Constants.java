package com.irefire.android.imdriving.utils;

public class Constants {
	
	public static final int SPEAK_TIME_OUT = 10 * 1000; // 10 s
	
	public static final int LISTEN_YES_NO_TIME_OUT = 3 * 1000; // 3 s
	
	public static final String NOTIFICATION_ENABLED = "com.irefire.android.imdriving.utils.NOTIFICATION_ENABLED";

    public static final String READ_APP_SAVED_FILE_NAME = "readapp.xml";

    public static final String TIMER_RECORD_SAVED_FILE_NAME = "timerrecord.xml";

    /*
    public static final long ONE_DAY = 5 * 60 * 1000;//24 * 3600 * 1000; // 一天的毫秒数。

    public static final long ONE_WEEK = 10 * 60 * 1000; //7 * ONE_DAY; // 一周

    public static final long ONE_HOUR = 60 * 1000;//3600 * 1000;
*/
    public static final long ONE_DAY = 24 * 3600 * 1000; // 一天的毫秒数。

    public static final long ONE_WEEK = 7 * ONE_DAY; // 一周

    public static final long ONE_HOUR = 3600 * 1000;
}
