package com.nana.bletool.callback;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import java.util.List;

import static com.nana.bletool.ui.ScanFragment.handler;
import static com.nana.bletool.ui.ScanFragment.scanListener;

/**
 * 扫描结果回调
 */
public class MyScanCallback extends ScanCallback {
    @Override
    public void onScanResult(int callbackType, final ScanResult result) {
        super.onScanResult(callbackType, result);
        handler.post(new Runnable() {
            @Override
            public void run() {
                scanListener.onScanSuccess(result);
            }
        });
    }

    @Override
    public void onScanFailed(final int errorCode) {
        super.onScanFailed(errorCode);
        handler.post(new Runnable() {
            @Override
            public void run() {
                scanListener.onScanFailed(errorCode);
            }
        });
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
    }
}