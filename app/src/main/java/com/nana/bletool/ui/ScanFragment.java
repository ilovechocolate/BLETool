package com.nana.bletool.ui;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.nana.bletool.Container;
import com.nana.bletool.adapter.BLEDeviceAdapter;
import com.nana.bletool.MainActivity;
import com.nana.bletool.MyHandler;
import com.nana.bletool.R;
import com.nana.bletool.ToastUtils;
import com.nana.bletool.bean.BLEDevice;
import com.nana.bletool.callback.MyScanCallback;
import com.nana.bletool.listener.MyScanListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.le.ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED;

/**
 * 扫描、连接、通信数据解析操作类
 */
public class ScanFragment extends Fragment {

    public Context context;

    private SwipeRefreshLayout refresh;
    private RecyclerView list;
    public BLEDeviceAdapter bleDeviceAdapter;
    public List<BLEDevice> bleDevices;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    // 扫描器
    private BluetoothLeScanner bluetoothLeScanner;
    // 扫描设置
    private ScanSettings scanSettings;
    // 扫描过滤器
    private List<ScanFilter> scanFilters;
    // 扫描回调
    private ScanCallback scanCallback;
    // 扫描回调监听
    public static MyScanListener scanListener;

    private int hintTimeout = 3*1000, scanTimeout = 30*1000;
    private int REQUEST_BLUETOOTH_ENABLE = 300;
    private boolean isScanning = false;

    public static MyHandler handler;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_scan, container, false);
        context = getContext();
        initBLE();
        handler = new MyHandler((MainActivity) getActivity());
        initView(root);
        getBLEDevices();
        return root;
    }

    private void initBLE() {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            assert bluetoothManager != null;
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter != null) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                scanCallback = new MyScanCallback();
                bleDevices = new ArrayList<>();
            } else {
                ToastUtils.showShort(context, "BLUETOOTH_LE open error");
            }
        } else {
            ToastUtils.showShort(context, "BLUETOOTH_LE is not supported");
        }
    }

    private void initView(final View view) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.findViewById(R.id.swipe).setVisibility(View.GONE);
            }
        }, hintTimeout);
        refresh = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ToastUtils.showShort(context, "refreshing...");
                getBLEDevices();
            }
        });
        list = (RecyclerView) view.findViewById(R.id.list);

        bleDeviceAdapter = new BLEDeviceAdapter(context, bleDevices);

        list.setLayoutManager(new LinearLayoutManager(context));
        list.setAdapter(bleDeviceAdapter);
        initListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }

    private void initListener() {
        scanListener = new MyScanListener() {
            @Override
            public void onScanSuccess(ScanResult result) {
                refreshBLEDeviceList(result);
            }

            @Override
            public void onScanFailed(int errorCode) {
                switch (errorCode) {
                    case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                        ToastUtils.showShort(context, "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED");
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void refreshBLEDeviceList(ScanResult scanResult) {
        BLEDevice bleDevice = new BLEDevice(scanResult.getDevice(), scanResult.getScanRecord(), scanResult.getRssi());
        for (int i = 0; i < bleDevices.size(); i++) {
            if (bleDevice.device.getAddress().equals(bleDevices.get(i).device.getAddress())) {
                bleDevices.set(i, bleDevice);
                bleDeviceAdapter.notifyItemChanged(i);
                return;
            }
        }
        bleDevices.add(bleDevice);
        bleDeviceAdapter.notifyItemInserted(bleDevices.size()-1);
    }

    private void getBLEDevices() {
        bleDevices.clear();
        bleDeviceAdapter.notifyDataSetChanged();
        startScan(scanTimeout);
    }

    private void startScan(int timeout) {
        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
        } else {
            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            }
            if (!isScanning) {
                isScanning = true;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort(context, "start scanning...");
                        initScanFilters();
                        initScanSettings();
                        // 开始扫描
                        // 直接扫描
                        bluetoothLeScanner.startScan(scanCallback);
                        // 设置拦截器和扫描选项
//                        bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
                    }
                });
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopScan();
                    }
                }, timeout);
            } else {
                refresh.setRefreshing(false);
            }
        }
    }

    private void initScanFilters() {
        scanFilters = new ArrayList<>();
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setDeviceName("lalala")
                .setServiceUuid(new ParcelUuid(UUID.fromString(Container.TEST_UUID)))
                .build();
        scanFilters.add(scanFilter);
    }

    @TargetApi(23)
    private void initScanSettings() {
        scanSettings = new ScanSettings.Builder()
                // 设置扫描模式
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                // 设置回调类型
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                // 设置配对模式
                .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
                // 设置报告延迟
                .setReportDelay(0)
                .build();
    }

    private void stopScan() {
        if (isScanning) {
            ToastUtils.showShort(context, "stop scanning...");
            handler.removeCallbacks(null);
            isScanning = false;
            bluetoothLeScanner.stopScan(scanCallback);
            refresh.setRefreshing(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
            startScan(scanTimeout);
        }
    }
}