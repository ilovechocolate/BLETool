package com.nana.bletool;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nana.bletool.adapter.BLEServiceAdapter;
import com.nana.bletool.bean.BLEData;
import com.nana.bletool.bean.BLEService;
import com.nana.bletool.callback.MyBluetoothGattCallback;
import com.nana.bletool.listener.MyBluetoothGattListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.bluetooth.BluetoothGatt.GATT_READ_NOT_PERMITTED;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

public class BLEConnectionActivity extends Activity {

    public static Context context;
    String address;
    List<BLEData> bleDataList;

    private Button connect;
    private TextView manu_data;
    private SwipeRefreshLayout service_refresh;
    private RecyclerView service_view;
    private BLEServiceAdapter bleServiceAdapter;
    private List<BLEService> serviceList;

    boolean isConnected = false;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    // 远端蓝牙设备（连接的设备）
    BluetoothDevice bluetoothDevice;
    // 蓝牙连接对象
    static BluetoothGatt bluetoothGatt;
    // 蓝牙连接回调
    BluetoothGattCallback bluetoothGattCallback;
    // 蓝牙连接回调监听
    public static MyBluetoothGattListener bluetoothGattListener;

    static TextView characteristicValue, descriptorValue;

    public static MyHandler handler;

    private int retry = 0, retryCount = 3;
    private int REQUEST_BLUETOOTH_ENABLE = 400;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        context = this;
        parseIntent();
        handler = new MyHandler(this);
        initBLE();
        initView();
        iniListener();
        connectBLE();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String data = intent.getStringExtra("data");
            address = intent.getStringExtra("address");
            if (!TextUtils.isEmpty(data) && !TextUtils.isEmpty(address)) {
                bleDataList = BLEUtils.parseBleData(data);
                return;
            }
        }
        ToastUtils.showShort(context, "BLEConnectionActivity parseIntent error");
        finish();
    }

    private void initBLE() {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            assert bluetoothManager != null;
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter != null) {
                bluetoothGattCallback = new MyBluetoothGattCallback();
                serviceList = new ArrayList<>();
            } else {
                ToastUtils.showLong(context, "BLUETOOTH_LE open error");
            }
        } else {
            ToastUtils.showLong(context, "BLUETOOTH_LE is not supported");
        }
    }

    private void initView() {
        connect = (Button) findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect.setClickable(false);
                if (isConnected) {
                    disconnectBLE();
                } else {
                    connectBLE();
                }
            }
        });
        manu_data = (TextView) findViewById(R.id.manu_data);
        showData();
        service_refresh = (SwipeRefreshLayout) findViewById(R.id.service_refresh);
        service_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                discoverServices();
            }
        });
        service_view = (RecyclerView) findViewById(R.id.service_view);
        bleServiceAdapter = new BLEServiceAdapter(context, serviceList, false);
        service_view.setLayoutManager(new LinearLayoutManager(context));
        service_view.setAdapter(bleServiceAdapter);
    }

    private void iniListener() {
        bluetoothGattListener = new MyBluetoothGattListener() {
            @Override
            public void onConnectionStateChange(int status, int newState) {
                switch (status) {
                    case BluetoothGatt.GATT_SUCCESS:
                        switch (newState) {
                            case BluetoothProfile.STATE_DISCONNECTED:
                                connect.setText("CONNECT");
                                connect.setClickable(true);
                                isConnected = false;
                                clearServices();
                                closeBLE();
                                break;
                            case BluetoothProfile.STATE_CONNECTED:
                                retry = 0;
                                connect.setText("DISCONNECT");
                                connect.setClickable(true);
                                isConnected = true;
                                discoverServices();
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        ToastUtils.showLong(context, "GATT_FAILED!");
                        if (!isConnected) {
                            if (retry < retryCount) {
                                reconnectBLE();
                            } else {
                                closeBLE();
                            }
                        }
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(int status) {
                service_refresh.setRefreshing(false);
                service_refresh.setEnabled(true);
                switch (status) {
                    case GATT_SUCCESS:
                        serviceList.clear();
                        List<BluetoothGattService> services = bluetoothGatt.getServices();
                        for (int i = 0; i < services.size(); i++) {
                            serviceList.add(new BLEService(services.get(i)));
                        }
                        bleServiceAdapter.notifyDataSetChanged();
                        break;
                    default:
                        ToastUtils.showShort(context, "GATT_FAILED!");
                        break;
                }
            }

            /**
             * 服务端（广播方）调用写特征后回调
             * todo 服务端修改描述无相应回调，需客户端主动读取
             * @param characteristic
             */
            @Override
            public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
                characteristicValue.setText("Value : " + new String(characteristic.getValue()));
            }

            /**
             * 客户端（连接方）调用读特征后回调
             * @param characteristic
             * @param status
             */
            @Override
            public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
                switch (status) {
                    case GATT_SUCCESS:
                        String valueStr = new String(characteristic.getValue());
                        characteristicValue.setText("Value : " + valueStr);
                        ToastUtils.showShort(context, "getValue = " + valueStr);
                        break;
                    case GATT_READ_NOT_PERMITTED:
                        ToastUtils.showShort(context, "GATT_READ_NOT_PERMITTED");
                        break;
                    default:
                        Log.d(Container.TAG, "onCharacteristicRead failed! status = " + status);
                        ToastUtils.showShort(context, "CHARACTERISTIC_READ_FAILED");
                        break;
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
                switch (status) {
                    case GATT_SUCCESS:
                        String valueStr = new String(characteristic.getValue());
                        characteristicValue.setText("Value : " + valueStr);
                        ToastUtils.showShort(context, "setValue = " + valueStr);
                        break;
                    default:
                        Log.d(Container.TAG, "onCharacteristicWrite failed! status = " + status);
                        ToastUtils.showShort(context, "CHARACTERISTIC_WRITE_FAILED");
                        break;
                }
            }

            @Override
            public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status) {
                switch (status) {
                    case GATT_SUCCESS:
                        String valueStr = new String(descriptor.getValue());
                        descriptorValue.setText("Value : " + valueStr);
                        ToastUtils.showShort(context, "getValue = " + valueStr);
                        break;
                    case GATT_READ_NOT_PERMITTED:
                        ToastUtils.showShort(context, "GATT_READ_NOT_PERMITTED");
                        break;
                    default:
                        Log.d(Container.TAG, "onDescriptorRead failed! status = " + status);
                        ToastUtils.showShort(context, "DESCRIPTOR_READ_FAILED");
                        break;
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
                switch (status) {
                    case GATT_SUCCESS:
                        String valueStr = new String(descriptor.getValue());
                        descriptorValue.setText("Value : " + valueStr);
                        ToastUtils.showShort(context, "setValue = " + valueStr);
                        break;
                    default:
                        Log.d(Container.TAG, "onDescriptorWrite failed! status = " + status);
                        ToastUtils.showShort(context, "DESCRIPTOR_WRITE_FAILED");
                        break;
                }
            }
        };
    }

    private void showData() {
        for (BLEData bleData : bleDataList) {
            switch (bleData.type.toUpperCase()) {
                case "FF":// 厂商数据
                    manu_data.setText("manu id = 0x" + bleData.data.substring(2, 4) + bleData.data.substring(0, 2));
                    manu_data.append("\nmanu data = 0x" + bleData.data.substring(4));
                    break;
                case "16":// 16-bit服务数据
                case "20":// 32-bit服务数据
                    Log.d(Container.TAG, "service uuid = 0x" + bleData.data.substring(2, 4) + bleData.data.substring(0, 2));
                    Log.d(Container.TAG, "service data = 0x" + bleData.data.substring(4));
                    break;
                case "03":
                    for (int i = 0; i < bleData.data.length() / 4; i++) {
                        Log.d(Container.TAG, "16-bit uuid[" + i + "] = 0x"
                                + bleData.data.substring(2 + 4 * i, 4 + 4 * i)
                                + bleData.data.substring(4 * i, 2 + 4 * i));
                    }
                    break;
                case "05":
                    for (int i = 0; i < bleData.data.length() / 8; i++) {
                        Log.d(Container.TAG, "32-bit uuid[" + i + "] = 0x"
                                + bleData.data.substring(6 + 8 * i, 8 + 8 * i)
                                + bleData.data.substring(4 + 4 * i, 6 + 4 * i)
                                + bleData.data.substring(2 + 4 * i, 4 + 4 * i)
                                + bleData.data.substring(4 * i, 2 + 4 * i));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 请求连接
     */
    private void connectBLE() {
        ToastUtils.showShort(context, "connecting...");
        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
        } else {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            if (bluetoothDevice != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            bluetoothGatt = bluetoothDevice.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
                        } else {
                            bluetoothGatt = bluetoothDevice.connectGatt(context, false, bluetoothGattCallback);
                        }
                    }
                });
            }
        }
    }

    /**
     * 断开连接
     */
    private void disconnectBLE() {
        ToastUtils.showShort(context, "disconnecting...");
        bluetoothGatt.disconnect();
    }

    /**
     * 重连
     */
    private void reconnectBLE() {
        ToastUtils.showShort(context, "reconnecting...");
        retry++;
        closeBLE();
        connectBLE();
    }

    /**
     * 彻底关闭连接
     */
    private void closeBLE() {
        if (!isConnected) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }

    /**
     * 搜索蓝牙发送的服务
     */
    private void discoverServices() {
        if (isConnected) {
            ToastUtils.showShort(context, "discover services...");
            service_refresh.setRefreshing(true);
            service_refresh.setEnabled(false);
            bluetoothGatt.discoverServices();
        }
    }

    /**
     * 清理发现的服务
     */
    private void clearServices() {
        if (!isConnected) {
            service_refresh.setRefreshing(false);
            service_refresh.setEnabled(false);
            serviceList.clear();
            bleServiceAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 向服务端请求读取特征值
     * 注意这里需要注册特征通知，读取到后就会回调onCharacteristicRead
     * @param characteristic
     * @param textView
     */
    public static void readCharacteristic(BluetoothGattCharacteristic characteristic, TextView textView) {
        characteristicValue = textView;
        if (bluetoothGatt.readCharacteristic(characteristic)) {
            bluetoothGatt.setCharacteristicNotification(characteristic, true);
        } else {
            ToastUtils.showShort(context, "readCharacteristic failed!");
        }
    }

    /**
     * 修改特征值
     * 同样注册特征通知，写完后就会回调onCharacteristicWrite
     * @param characteristic
     */
    public static void writeCharacteristic(final BluetoothGattCharacteristic characteristic, TextView textView) {
        characteristicValue = textView;
        final EditText input = new EditText(context);
        showInputDialog(input, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                characteristic.setValue(value.getBytes());
                if (bluetoothGatt.writeCharacteristic(characteristic)) {
                    bluetoothGatt.setCharacteristicNotification(characteristic, true);
                } else {
                    ToastUtils.showShort(context, "writeDescriptor failed!");
                }
            }
        });
    }

    /**
     * 读取描述值
     * @param descriptor
     * @param textView
     */
    public static void readDescriptor(BluetoothGattDescriptor descriptor, TextView textView) {
        if (bluetoothGatt.readDescriptor(descriptor)) {
            descriptorValue = textView;
        } else {
            ToastUtils.showShort(context, "readDescriptor failed!");
        }
    }

    /**
     * 修改描述值
     * @param descriptor
     */
    public static void writeDescriptor(final BluetoothGattDescriptor descriptor, TextView textView) {
        descriptorValue = textView;
        final EditText input = new EditText(context);
        showInputDialog(input, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                descriptor.setValue(value.getBytes());
                if (!bluetoothGatt.writeDescriptor(descriptor)) {
                    ToastUtils.showShort(context, "writeDescriptor failed!");
                }
            }
        });
    }

    /**
     * 输入弹窗
     * @param input
     * @param listener
     */
    private static void showInputDialog(EditText input, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setTitle("input : ")
                .setView(input)
                .setPositiveButton("finish", listener)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
            connectBLE();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        serviceList.clear();
        if (isConnected) {
            closeBLE();
        }
    }
}