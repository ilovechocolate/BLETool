package com.nana.bletool.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nana.bletool.Container;
import com.nana.bletool.R;
import com.nana.bletool.ToastUtils;

/**
 * 基本操作类
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private Context context;

    private Switch locationPermission, bluetoothStatus;
    private Button getBluetoothInfo;
    private TextView bluetoothInfo;

    private int PERMISSION_SEETING = 100, REQUEST_LOCATION_PERMISSION_SEETING = 101, REQUEST_BLUETOOTH_ENABLE = 102;
    private String[] permissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    // 蓝牙管理类
    private BluetoothManager bluetoothManager;
    // 蓝牙设配器
    private BluetoothAdapter bluetoothAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        context = getContext();
        initView(root);
        requestPermissions(permissions, REQUEST_LOCATION_PERMISSION_SEETING);
        initBLE();
        return root;
    }

    private void initView(View view) {
        locationPermission = (Switch) view.findViewById(R.id.location);
        bluetoothStatus = (Switch) view.findViewById(R.id.ble);
        getBluetoothInfo = (Button) view.findViewById(R.id.getBluetoothInfo);
        bluetoothInfo = (TextView) view.findViewById(R.id.bluetoothinfo);
        locationPermission.setOnClickListener(this);
        bluetoothStatus.setOnClickListener(this);
        getBluetoothInfo.setOnClickListener(this);
    }

    private void initBLE() {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            assert bluetoothManager != null;
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter != null) {
                bluetoothStatus.setChecked(bluetoothAdapter.isEnabled());
            }
        } else {
            ToastUtils.showShort(context, "BLUETOOTH_LE is not supported");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.location:
                OpenPermissionSetting();
                break;
            case R.id.ble:
                if (bluetoothAdapter == null) {
                    return;
                } else {
                    bluetoothAdapter.setName(android.os.Build.BRAND);
                    if (bluetoothAdapter.isEnabled()) {
                        bluetoothAdapter.disable();
                        bluetoothStatus.setChecked(false);
                    } else {
                        // open bluetooth queitly
//                        bluetoothAdapter.enable();
                        // open bluetooth with a notification
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
                        bluetoothStatus.setChecked(true);
                    }
                }
                break;
            case R.id.getBluetoothInfo:
                if (bluetoothAdapter == null) {
                    return;
                } else {
                    bluetoothInfo.setText("BluetoothInfo");
                    bluetoothInfo.append("\n");
                    bluetoothInfo.append("name = " + bluetoothAdapter.getName());
                    bluetoothInfo.append("\n");
                    bluetoothInfo.append("address = " + bluetoothAdapter.getAddress() + "（Android 8.0+无法获取）");
                    bluetoothInfo.append("\n");
                    bluetoothInfo.append("state = " + bluetoothAdapter.getState());
                    bluetoothInfo.append("\n");
                    bluetoothInfo.append("scanmode = " + bluetoothAdapter.getScanMode());
                    bluetoothInfo.append("\n");
                    bluetoothInfo.append("isEnabled = " + bluetoothAdapter.isEnabled());
                }
                break;
            default:
                break;
        }
    }

    private void OpenPermissionSetting() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        startActivityForResult(intent, PERMISSION_SEETING);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_SEETING) {
            requestPermissions(permissions, REQUEST_LOCATION_PERMISSION_SEETING);
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length > 0) {
            int isCoarseLocationEnabled = context.checkSelfPermission(permissions[0]);
            if (isCoarseLocationEnabled == PackageManager.PERMISSION_GRANTED) {
                locationPermission.setChecked(true);
            } else {
                locationPermission.setChecked(false);
            }
        }
    }

    /**
     * 查看设备支持的硬件特性
     */
    private void getSystemFeatures() {
        FeatureInfo[] featureInfos = context.getPackageManager().getSystemAvailableFeatures();
        for (FeatureInfo featureInfo : featureInfos) {
            Log.d(Container.TAG, featureInfo.name);
        }
    }

}