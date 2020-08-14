package com.nana.bletool.listener;

import android.bluetooth.le.ScanResult;

public interface MyScanListener {
    void onScanSuccess(ScanResult result);
    void onScanFailed(int errorCode);
}
