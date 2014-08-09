package com.irefire.android.imdriving.parse;

/**
 * Created by lan on 8/8/14.
 */
public interface ParseOp extends ParseConf{

    public String CLASS_URL = BASE_URL + "/" + V + "/classes/";

    public String getClassUrl();

    public String getObjectUrl();
}
