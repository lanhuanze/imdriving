package com.irefire.android.imdriving.utils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import android.text.TextUtils;

public class AppSettings {
	
	/**
	 * Read the notification without 
	 */
	private boolean autoRead = true;
	
	private String ttsLanguage = "cmn-CHN"; //ÖÐÎÄ
	private String ttsVoice = "Samantha";
	
	private BitSet changeSet = new BitSet();
	
	
	
	public static final AppSettings getInstance() {
		return Holder._INST;
	}
	
	private AppSettings() {
		
	}
	
	private static final class Holder {
		public static final AppSettings _INST = new AppSettings();
	}

	public boolean isAutoRead() {
		return autoRead;
	}

	public void setAutoReadWithoutAsk(boolean autoRead) {
		if(this.autoRead != autoRead) {
			changeSet.set(SettingItem.AUTOREAD.ordinal());
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
	
	public static enum SettingItem {
		LANGUAGE, VOICE, AUTOREAD;
	}
}
