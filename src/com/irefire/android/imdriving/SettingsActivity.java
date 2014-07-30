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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by lan on 7/30/14.
 */

public class SettingsActivity extends PreferenceActivity {
    private static final Logger l = LoggerFactory.getLogger(SettingsActivity.class.getSimpleName());

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Set<AppItem> apps = this.getAllApps();
        Set<String> readApps = this.getReadApps();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, new FirstFragment(apps, readApps)).commit();
        }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
    }

    private static class FirstFragment extends PreferenceFragment {

        private Set<AppItem> apps;
        private Set<String> readApps;

        public FirstFragment(Set<AppItem> apps, Set<String> readApps) {
            this.apps = apps;
            this.readApps = readApps;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.addPreferencesFromResource(R.xml.app_settings);
            Set<CharSequence> pkgs = new HashSet<CharSequence>();
            Set<CharSequence> names = new HashSet<CharSequence>();

            for(AppItem ai: apps) {
                SpannableStringBuilder spanStringBuilder = new SpannableStringBuilder();
                DrawableMarginSpan icon = new DrawableMarginSpan(ai.icon);
                spanStringBuilder.append(ai.name);
                spanStringBuilder.setSpan(icon, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                names.add(spanStringBuilder);
                pkgs.add(ai.pkg);
            }

            MultiSelectListPreference preference = (MultiSelectListPreference)this.findPreference("setting_choose_app_to_read");
            preference.setEntries(names.toArray(new CharSequence[0]));
            preference.setEntryValues(pkgs.toArray(new CharSequence[0]));
            preference.setDefaultValue(readApps);
        }
    }

    private Set<String> getReadApps() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
       Set<String> apps = prefs.getStringSet("setting_choose_app_to_read", Collections.EMPTY_SET);
       return apps;
    }

    private Set<AppItem> getAllApps() {
        PackageManager pm = this.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);
        Set<AppItem> items = new HashSet<AppItem>();
        for(ResolveInfo ai: list) {
            AppItem item = new AppItem();
            item.pkg = ai.activityInfo.packageName;
            item.name = ai.activityInfo.loadLabel(pm);
            item.icon = ai.activityInfo.loadIcon(pm);
            items.add(item);
        }
        return items;
    }

    private static final class AppItem {
        public CharSequence pkg;
        public CharSequence name;
        public Drawable icon;

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }else if(o instanceof AppItem) {
                AppItem ai = (AppItem)o;
                return TextUtils.equals(pkg, ai.pkg) && TextUtils.equals(name, ai.name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return super.hashCode() + name.hashCode() + pkg.hashCode();
        }
    }
}