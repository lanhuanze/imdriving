package com.irefire.android.imdriving.utils;

public class AppSettings {
	
	/**
	 * Read the notification without 
	 */
	private boolean autoReadWithoutAsk = true;
	
	private String ttsLanguage = "en_US";
	private String ttsVoice = "Samantha";
	
	
	
	public static final AppSettings getInstance() {
		return Holder._INST;
	}
	
	private AppSettings() {
		
	}
	
	private static final class Holder {
		public static final AppSettings _INST = new AppSettings();
	}

	public boolean isAutoReadWithoutAsk() {
		return autoReadWithoutAsk;
	}

	public void setAutoReadWithoutAsk(boolean autoReadWithoutAsk) {
		this.autoReadWithoutAsk = autoReadWithoutAsk;
	}

	public String getTtsLanguage() {
		return ttsLanguage;
	}

	public void setTtsLanguage(String ttsLanguage) {
		this.ttsLanguage = ttsLanguage;
	}

	public String getTtsVoice() {
		return ttsVoice;
	}

	public void setTtsVoice(String ttsVoice) {
		this.ttsVoice = ttsVoice;
	}
	
	
}
