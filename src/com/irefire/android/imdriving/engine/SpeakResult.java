package com.irefire.android.imdriving.engine;

public class SpeakResult {
	public Engine.EngineResult result = Engine.EngineResult.OK;
	public String utteranceId;
	public long speakStartTime;
	public long speakEndTime;
    public boolean isSpeakFinish;

    @Override
    public String toString() {
        return "SpeakResult{" +
                "result=" + result +
                ", utteranceId='" + utteranceId + '\'' +
                ", speakStartTime=" + speakStartTime +
                ", speakEndTime=" + speakEndTime +
                ", isSpeakFinish=" + isSpeakFinish +
                '}';
    }
}
