package com.irefire.android.imdriving;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import com.irefire.android.imdriving.engine.SystemEngine;

public class App extends Application {

    private static Context sContext;

    public static Context getStaticContext() {
        assert sContext != null;
        return sContext;
    }

    @Override
    public void onCreate() {
        sContext = this;
        super.onCreate();
        //SystemEngine.isSystemEngineSupported(this);
        // android.speech.SpeechRecognizer.createSpeechRecognizer can be only invoked in
        // Main thread, so we call it here.
        SystemEngine.getInstance();
    }

    @Override
    public void onTerminate() {
        // TODO Auto-generated method stub
        super.onTerminate();
        sContext = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }
}
