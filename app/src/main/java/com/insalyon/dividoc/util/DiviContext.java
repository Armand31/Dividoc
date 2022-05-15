package com.insalyon.dividoc.util;

import android.app.Application;
import android.content.Context;

public class DiviContext extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        DiviContext.context = (Context) getApplicationContext();
    }

    public static Context getAppContext() {
        return DiviContext.context;
    }
}
