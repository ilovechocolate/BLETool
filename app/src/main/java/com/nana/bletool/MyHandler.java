package com.nana.bletool;

import android.app.Activity;
import android.os.Handler;

import java.lang.ref.WeakReference;

public class MyHandler extends Handler {
    private final WeakReference<Activity> activityWeakReference;

    public MyHandler(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }
}
