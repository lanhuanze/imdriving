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
import com.android.vending.billing.utils.Subscription;
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

    /**
     * 用户第一次使用我们软件的时间
     */
    private long firstUseTime = System.currentTimeMillis();

    private long lastUseTime = System.currentTimeMillis();

    public Subscription.Type subscriptionType;
    public Subscription.Status subscriptionStatus;

    private String accountInfoId = "";

    public Subscription.Status getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(Subscription.Status subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
        SharedPreferences.Editor e = mPrefs.edit();
        e.putString(KEY_SUBSCRIPTION_STATUS, subscriptionStatus.name());
        e.commit();
    }

    public Subscription.Type getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(Subscription.Type subscriptionType) {
        this.subscriptionType = subscriptionType;
        SharedPreferences.Editor e = mPrefs.edit();
        e.putString(KEY_SUBSCRIPTION_TYPE, subscriptionType.name());
        e.commit();
    }

    public String getAccountInfoId() {
        return accountInfoId;
    }

    public void setAccountInfoId(String accountInfoId) {
        this.accountInfoId = accountInfoId;
        SharedPreferences.Editor e = mPrefs.edit();
        e.putString(KEY_ACCOUNT_INFO_ID, accountInfoId);
        e.commit();
    }

    private SharedPreferences mPrefs = null;
	
	public static final AppSettings getInstance() {
		return Holder._INST;
	}
	
	private AppSettings() {
		mContext = App.getStaticContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        setAllowedPackages(getAllowedPackages());
        removeAnnoyPackages();

        loadSettings();
    }
	
	private static final class Holder {
		public static final AppSettings _INST = new AppSettings();
	}

    public void loadSettings() {
        this.autoRead = mPrefs.getBoolean(KEY_APP_SETTINGS_AUTO_READ_STATE, false);
        this.ttsLanguage = mPrefs.getString(KEY_APP_SETTINGS_LANGUAGE, "en_US");
        this.firstUseTime = mPrefs.getLong(KEY_FIRST_USED_TIME, System.currentTimeMillis());
        this.lastUseTime = mPrefs.getLong(KEY_LAST_USED_TIME, System.currentTimeMillis());
        this.subscriptionStatus = Subscription.Status.valueOf(mPrefs.getString(KEY_SUBSCRIPTION_STATUS, Subscription.Status.NONE.name()));
        this.subscriptionType = Subscription.Type.valueOf(mPrefs.getString(KEY_SUBSCRIPTION_TYPE, Subscription.Type.NONE.name()));
        this.accountInfoId = mPrefs.getString(KEY_ACCOUNT_INFO_ID, "");
        l.debug("Language:" + ttsLanguage);
        l.debug("AutoRead:" + autoRead);
        l.debug("firstUseTime:" + new Date(firstUseTime));
        l.debug("lastUseTime:" + new Date(lastUseTime));
        l.debug("subscriptionStatus:" + subscriptionStatus);
        l.debug("subscriptionType:" + subscriptionType);
        l.debug("accountInfoId:" + accountInfoId);
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

    public long getFirstUseTime() {
        return firstUseTime;
    }

    public void setFirstUseTime(long firstUseTime) {
        this.firstUseTime = firstUseTime;
       SharedPreferences.Editor e = mPrefs.edit();
        e.putLong(KEY_FIRST_USED_TIME, firstUseTime);
        e.commit();
    }

    public long getLastUseTime() {
        return lastUseTime;
    }

    public void setLastUseTime(long lastUseTime) {
        this.lastUseTime = lastUseTime;
        SharedPreferences.Editor e = mPrefs.edit();
        e.putLong(KEY_LAST_USED_TIME, lastUseTime);
        e.commit();
    }

    private List<String> mAllowedPackages = new ArrayList<String>();

    public boolean getServiceStarted() {
        return mServiceStarted;
    }

    public void setServiceStarted(boolean started) {
        mServiceStarted = started;
    }

    private static final String KEY_APP_SETTINGS_AUTO_READ_STATE = "app_settings_auto_read_state";
    private static final String KEY_APP_SETTINGS_LANGUAGE = "app_settings_language";
    private static final String KEY_FIRST_USED_TIME = "first_used_time";
    private static final String KEY_LAST_USED_TIME = "last_used_time";
    private static final String KEY_SUBSCRIPTION_TYPE = "subscription_type";
    private static final String KEY_SUBSCRIPTION_STATUS = "subscription_status";
    private static final String KEY_ACCOUNT_INFO_ID = "account_info_id";
}
