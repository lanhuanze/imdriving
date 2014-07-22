package com.irefire.android.imdriving.service;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

import com.irefire.android.imdriving.App;
import com.irefire.android.imdriving.R;
import com.irefire.android.imdriving.utils.AppSettings;
import com.irefire.android.imdriving.utils.AppSettings.SettingChangeListener;
import com.irefire.android.imdriving.utils.AppSettings.SettingItem;

public class ResourceManager implements SettingChangeListener {

	private static final Logger l = LoggerFactory
			.getLogger(ResourceManager.class.getSimpleName());

	private Map<String, String> cachedPackageNames = new ConcurrentHashMap<String, String>();

	private Context mContext;

	private PackageManager pm = null;

	private List<String> positiveWords;
	private List<String> negativeWords;
	private List<String> stopWords;

	private BitSet mCheckBitSet = new BitSet();

	private static final class Holder {
		public static final ResourceManager _INST = new ResourceManager();
	}

	public static ResourceManager getInstance() {
		return Holder._INST;
	}

	private ResourceManager() {
		mContext = App.getStaticContext();
		pm = mContext.getPackageManager();
		mCheckBitSet.set(SettingItem.LANGUAGE.ordinal());
		positiveWords = Arrays.asList(mContext.getResources().getStringArray(
				R.array.positive_words));
		negativeWords = Arrays.asList(mContext.getResources().getStringArray(
				R.array.negative_words));
		stopWords = Arrays.asList(mContext.getResources().getStringArray(
				R.array.stop_words));
		AppSettings.getInstance().addListener(this);
	}

	@Override
	public void onChange(AppSettings settings, BitSet set) {
		if (set.intersects(mCheckBitSet)) {
			l.debug("clear all the cached app names since app settings changed.");
			cachedPackageNames.clear();
		}
	}

	public String getAppName(String pkgName) {
		if (TextUtils.isEmpty(pkgName)) {
			l.debug("try to get app name with pkgName is empty");
			return "";
		}
		String name = cachedPackageNames.get(pkgName);
		if (!TextUtils.isEmpty(name)) {
			l.debug("Get app name from cache pkgName:" + pkgName + ",name:"
					+ name);
			return name;
		}
		try {
			ApplicationInfo app = pm.getApplicationInfo(pkgName, 0);
			name = pm.getApplicationLabel(app).toString();
			l.debug("Get a new app name and put it to cache, pkgName:"
					+ pkgName + ", name:" + name);
			if (!TextUtils.isEmpty(name)) {
				cachedPackageNames.put(pkgName, name);
			}
		} catch (NameNotFoundException e) {
			l.warn("getAppName got exception:" + e);
		}
		return name;
	}
	
	public boolean wordPositive(String word) {
		return positiveWords.contains(word);
	}

	public boolean wordNegative(String word) {
		return negativeWords.contains(word);
	}
	
	public boolean wordStop(String word) {
		return stopWords.contains(word);
	}

    public String getString(int resId) {
        return mContext.getString(resId);
    }

    public String getString(int resId, Object ... args) {
        return mContext.getString(resId, args);
    }
}
