package com.irefire.android.imdriving.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

        setAllowedPackages(getAllowedPackages());
        removeAnnoyPackages();

        loadSettings();
    }
	
	private static final class Holder {
		public static final AppSettings _INST = new AppSettings();
	}

    public void loadSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        this.autoRead = prefs.getBoolean("app_settings_auto_read_state", false);
        this.ttsLanguage = prefs.getString("app_settings_language", "en_US");
        l.debug("Language:" + ttsLanguage);
        l.debug("AutoRead:" + autoRead);
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

    public boolean allowedReadNotification(String packageName) {
        return mAllowedPackages.contains(packageName);
    }

    public boolean updateAllowedPackages(Map<String, Boolean> maps) {
        if(maps == null || maps.size() <= 0) {
            return false;
        }
        synchronized (mAllowedPackages) {
            mAllowedPackages.clear();
            for(String p: maps.keySet()) {
                if(Boolean.valueOf(maps.get(p) ==  null ? "false":"true")) {
                    mAllowedPackages.add(p);
                }
            }
        }
        return true;
    }

    public boolean setAllowedPackages(List<String> pkgs) {
        synchronized (mAllowedPackages) {
            mAllowedPackages.clear();
            mAllowedPackages.addAll(pkgs);
        }
        return true;
    }

    public List<String> getAllowedPackages() {
        try {
            InputStream in = mContext.openFileInput(Constants.READ_APP_SAVED_FILE_NAME);
            byte[] data = new byte[in.available()];
            in.read(data);
            String content = new String(data);
            String[] pkgs = content.split(";");
            return Arrays.asList(pkgs);
        }  catch (FileNotFoundException e) {
            l.warn("Error when read read app:" + e.getMessage());
        }catch(IOException e) {
            l.warn("Error when read read app:" + e.getMessage());
        }
       return  Collections.emptyList();
    }

    public boolean saveReadApps(Map<String, Boolean> maps) {
        StringBuilder builder = new StringBuilder(2048);
        for(String pkg: maps.keySet()) {
            if(maps.get(pkg)) {
                builder.append(pkg);
                builder.append(";");
            }
        }
        boolean result = true;
        try {
            OutputStream out = mContext.openFileOutput(Constants.READ_APP_SAVED_FILE_NAME, Context.MODE_PRIVATE);
            out.write(builder.toString().getBytes());
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            l.warn("Error when save read app:" + e.getMessage());
            result = false;
        }catch(IOException e) {
            l.warn("Error when save read app:" + e.getMessage());
            result = false;
        }
        return result;
    }

    private void removeAnnoyPackages() {
        synchronized (mAllowedPackages) {
            mAllowedPackages.remove("");
        }
    }

    private List<String> mAllowedPackages = new ArrayList<String>();

    public boolean getServiceStarted() {
        return mServiceStarted;
    }

    public void setServiceStarted(boolean started) {
        mServiceStarted = started;
    }
}
