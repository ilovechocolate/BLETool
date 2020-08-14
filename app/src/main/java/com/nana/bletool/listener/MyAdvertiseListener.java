package com.nana.bletool.listener;

import android.bluetooth.le.AdvertiseSettings;

public interface MyAdvertiseListener {
    void onStartSuccess(AdvertiseSettings settingsInEffect);
    void onStartFailure(int errorCode);
}
