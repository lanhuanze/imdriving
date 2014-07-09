package com.irefire.android.imdriving.engine;

import com.irefire.android.imdriving.event.Event;

public interface Engine {
	
	public static enum EngineResult {
		OK, FAILED, INVALID_PARAM, UNSUPPORT_LANGUAGE, NETWORK_ERROR;
	}
	
	/**
	 * TTS 一段文字, 同步的，要等说完了函数才返回
	 * @param text 要朗读的文字
	 * @param target 用户的
	 * @return
	 */
	public EngineResult speak(String text, Event e);
	
	/**
	 * 听写文字，要等有识别结果了才返回
	 * @param target
	 * @param timeout 超时时间， -1表示由识别引擎来决定是否结束听写。
	 * @return
	 */
	public DictationResult dictateText(Event e, long timeout);
}
