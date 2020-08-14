package com.nana.bletool.callback;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;

import static com.nana.bletool.ui.AdvertiseFragment.advertiseListener;
import static com.nana.bletool.ui.AdvertiseFragment.handler;

/**r
 * 广播操作回调类
 */
public class MyAdvertiseCallback extends AdvertiseCallback {
    @Override
    public void onStartSuccess(final AdvertiseSettings settingsInEffect) {
        super.onStartSuccess(settingsInEffect);
        handler.post(new Runnable() {
            @Override
            public void run() {
                advertiseListener.onStartSuccess(settingsInEffect);
            }
        });
    }

    @Override
    public void onStartFailure(final int errorCode) {
        super.onStartFailure(errorCode);
        handler.post(new Runnable() {
            @Override
            public void run() {
                advertiseListener.onStartFailure(errorCode);
            }
        });
    }
}







