package com.irefire.android.imdriving.utils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.irefire.android.imdriving.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppSettings {

    private static final Logger l = LoggerFactory.getLogger(AppSettings.class.getSimpleName());

	/**
	 * Read the notification without 
	 */
	private boolean autoRead = false;
	
	private String ttsLanguage = "cmn-CHN"; //中文
	private String ttsVoice = "Samantha";
	
	private Locale mLocale = Locale.US;

    private boolean mServiceStarted = false;
	
	private BitSet changeSet = new BitSet();
	private Context mContext = null;
	
	
	public static final AppSettings getInstance() {
		return Holder._INST;
	}
	
	private AppSettings() {
		mContext = App.getStaticContext();
        mIgnorePackages.addAll(this.getInputMethodPackages());
        // we don't read the notification from android system.
        mIgnorePackages.add("android");
        mIgnorePackages.add("com.android.providers.downloads");
    }
	
	private static final class Holder {
		public static final AppSettings _INST = new AppSettings();
	}

	public boolean isAutoRead() {
		return autoRead;
	}

	public void setAutoReadWithoutAsk(boolean autoRead) {
		if(this.autoRead != autoRead) {
			changeSet.set(SettingItem.AUTO_READ.ordinal());
		}
		this.autoRead = autoRead;
	}

	public String getTtsLanguage() {
		return ttsLanguage;
	}

	public void setTtsLanguage(String ttsLanguage) {
		if(!TextUtils.equals(this.ttsLanguage, ttsLanguage)) {
			changeSet.set(SettingItem.LANGUAGE.ordinal());
		}
		this.ttsLanguage = ttsLanguage;
	}

	public String getTtsVoice() {
		return ttsVoice;
	}

	public void setTtsVoice(String ttsVoice) {
		if(!TextUtils.equals(this.ttsVoice, ttsVoice)) {
			changeSet.set(SettingItem.VOICE.ordinal());
		}
		this.ttsVoice = ttsVoice;
	}
	
	public static interface SettingChangeListener {
		public void onChange(AppSettings settings, BitSet set);
	}
	
	private List<SettingChangeListener> mListeners = new ArrayList<SettingChangeListener>();
	
	public void addListener(SettingChangeListener listener) {
		if(listener == null) {
			return;
		}
		synchronized(mListeners) {
			mListeners.add(listener);
		}
	}
	
	public boolean removeListener(SettingChangeListener listener) {
		if(listener == null) {
			return false;
		}
		synchronized(mListeners) {
			return mListeners.remove(listener);
		}
	}
	
	public void notifyChange() {
		 if(!changeSet.isEmpty()) {
			 synchronized(mListeners) {
				 for(SettingChangeListener l: mListeners) {
					 l.onChange(this, changeSet);
				 }
			 }
		 }
		 changeSet.clear();
	}
	
	public Locale getLocale() {
		return mLocale;
	}

	public void setLocale(Locale mLocale) {
		this.mLocale = mLocale;
	}

	public static enum SettingItem {
		LANGUAGE, VOICE, AUTO_READ;
	}

    /**
     * check if ignore the notification from this package.
     * Such as InputMethod, DownloadManager etc.
     * @param packageName
     * @return
     */
    public boolean ignoreNotification(String packageName) {
        return mIgnorePackages.contains(packageName);
    }

    private List<String> mIgnorePackages = new ArrayList<String>();

    private List<String> getInputMethodPackages() {
        List<String> list = new ArrayList<String>();
        InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> imis = imm.getInputMethodList();
        for(InputMethodInfo imi: imis) {
            list.add(imi.getPackageName());
            l.debug("InputMethod:" + imi.getPackageName());
        }
        return list;
    }

    public boolean getServiceStarted() {
        return mServiceStarted;
    }

    public void setServiceStarted(boolean started) {
        mServiceStarted = started;
    }
}
