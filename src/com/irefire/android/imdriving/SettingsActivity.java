package com.irefire.android.imdriving;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.*;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.DrawableMarginSpan;
import com.irefire.android.imdriving.utils.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by lan on 7/30/14.
 */

public class SettingsActivity extends PreferenceActivity {
    private static final Logger l = LoggerFactory.getLogger(SettingsActivity.class.getSimpleName());

    private AppSettings mSettings = null;
    private PreferenceFragment mFragment = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mFragment = new FirstFragment(this);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, mFragment).commit();
        }
        mSettings = AppSettings.getInstance();
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
    }

    private static class FirstFragment extends PreferenceFragment {

        private SettingsActivity mHost;
        public FirstFragment(SettingsActivity host) {
            mHost = host;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.addPreferencesFromResource(R.xml.app_settings);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // refresh settings.
        mSettings.loadSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}