package com.irefire.android.imdriving.utils;

import android.content.Context;
import android.text.TextUtils;
import com.irefire.android.imdriving.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lan on 8/23/14.
 */
public class TimerRecorder {

    private static final Logger l = LoggerFactory.getLogger(TimerRecorder.class.getSimpleName());

    public static final TimerRecorder getInstance() {
        return Holder._INST;
    }

    private static final class Holder {
        public static final TimerRecorder _INST = new TimerRecorder();
    }

    private TimerRecorder() {
        loadTimerRecords();
    }

    public void start() {
        cur = new RecordItem();
        cur.startTime = System.currentTimeMillis();
    }

    public void stop() {
        if(cur != null) {
            cur.stopTime = System.currentTimeMillis();
            addRecordItem(cur);
            cur = null;
            saveTimerRecords();
        }
    }

    public boolean readQuota() {
        long total = 0;
        synchronized (recordItems) {
            for(RecordItem r: recordItems) {
                if(r.stopTime + Constants.ONE_DAY < System.currentTimeMillis()) {
                    recordItems.remove(r);
                }else {
                    total += (r.stopTime -  r.startTime);
                }
            }
        }
        saveTimerRecords();
        return (total > Constants.ONE_HOUR);
    }

    private static final class RecordItem implements Comparable<RecordItem>{
        public long startTime;
        public long stopTime;

        public RecordItem() {
            startTime = System.currentTimeMillis();
            stopTime = System.currentTimeMillis();
        }

        public RecordItem(String s) {
            l.debug("Create item with string:" + s);
            String[] parts = s.split(":");
            startTime = Long.parseLong(parts[0]);
            stopTime = Long.parseLong(parts[1]);
            l.debug("Created item:" + this);
        }

        @Override
        public int compareTo(RecordItem o) {
            int compare = Systems.compare(startTime, o.startTime);
            if(compare != 0) {
                return compare;
            }
            return Systems.compare(stopTime, o.stopTime);
        }

        public StringBuilder toSaveString() {
            StringBuilder builder = new StringBuilder();
            builder.append(startTime);
            builder.append(":");
            builder.append(stopTime);
            l.debug("Save item with string:" + builder.toString());
            l.debug("Save item:" + this);
            return builder;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(64);
            builder.append("start:" + new Date(startTime));
            builder.append("stopTime:" + new Date(stopTime));
            return builder.toString();
        }
    }

    private RecordItem cur;
    private List<RecordItem> recordItems = new ArrayList<RecordItem>();

    private boolean addRecordItem(RecordItem ri) {
        synchronized (recordItems) {
            for(RecordItem r: recordItems) {
                if(r.stopTime + Constants.ONE_DAY < System.currentTimeMillis()) {
                    recordItems.remove(r);
                }
            }
            recordItems.add(ri);
        }
        return true;
    }

    private void loadTimerRecords() {
        try {
            InputStream in = App.getStaticContext().openFileInput(Constants.READ_APP_SAVED_FILE_NAME);
            byte[] data = new byte[in.available()];
            in.read(data);
            String content = new String(data);
            String[] items = content.split(";");
            for(String item : items) {
                if(!TextUtils.isEmpty(item) && item.indexOf(':') != -1) {
                    addRecordItem(new RecordItem(item));
                }
            }
        }catch(IOException e) {

        }
    }

    private void saveTimerRecords() {
        StringBuilder builder = new StringBuilder();
        synchronized (recordItems) {
            for(RecordItem r: recordItems) {
                if(r.stopTime + Constants.ONE_DAY < System.currentTimeMillis()) {
                    recordItems.remove(r);
                }else {
                    builder.append(r.toSaveString());
                    builder.append(";");
                }
            }
        }
        try {
            OutputStream os = App.getStaticContext().openFileOutput(Constants.TIMER_RECORD_SAVED_FILE_NAME, 0);
            os.write(builder.toString().getBytes());
            os.flush();
            os.close();
        }catch(IOException e) {

        }
    }
}
