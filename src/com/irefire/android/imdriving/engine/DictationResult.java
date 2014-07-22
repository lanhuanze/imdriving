package com.irefire.android.imdriving.engine;

import java.util.ArrayList;
import java.util.List;

public class DictationResult {
    public List<ResultText> texts = new ArrayList<ResultText>();
    public Engine.EngineResult result = Engine.EngineResult.OK;

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer(512);
        buffer.append("result:");
        buffer.append(result.name());
        if (texts.isEmpty()) {
            buffer.append("No dictate result.");
        } else {
            for (ResultText rt : texts) {
                buffer.append("\n");
                buffer.append("text:");
                buffer.append(rt.getText());
                buffer.append(",score:");
                buffer.append(rt.getScore());
            }
        }
        return buffer.toString();
    }
}
