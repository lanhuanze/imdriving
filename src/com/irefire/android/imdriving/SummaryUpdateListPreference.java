package com.irefire.android.imdriving;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * Created by Huanze.Lan on 8/1/2014.
 */
public class SummaryUpdateListPreference extends ListPreference {
    public SummaryUpdateListPreference(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public SummaryUpdateListPreference(Context context) {
        super(context);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if(positiveResult) {
            this.setSummary(getEntry());
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        super.onSetInitialValue(restoreValue, defaultValue);
        this.setSummary(getEntry());
    }
}
