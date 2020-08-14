package com.nana.bletool.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nana.bletool.Container;
import com.nana.bletool.adapter.BLEServiceAdapter;
import com.nana.bletool.bean.BLEService;
import com.nana.bletool.callback.MyAdvertiseCallback;
import com.nana.bletool.MyHandler;
import com.nana.bletool.R;
import com.nana.bletool.ToastUtils;
import com.nana.bletool.callback.MyBluetoothGattServerCallback;
import com.nana.bletool.listener.MyAdvertiseListener;
import com.nana.bletool.listener.MyBluetoothGattServerListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.le.AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED;
import static android.bluetooth.le.AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE;

/**
 * 广播操作类
 */
public class AdvertiseFragment extends Fragment implements View.OnClickListener{

    private static Context context;

    private Switch advertise_state, connecttable, connect_state;
    private Button advertise_btn, server_btn, add_service;
    private TextView device_addr;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    // 广播操作类
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    // 广播设置类
    private AdvertiseSettings advertiseSettings;
    // 广播报文类
    private AdvertiseData advertiseData, advertiseResData;
    // 广播回调类
    private MyAdvertiseCallback advertiseCallback;
    // 广播监听类
    public static MyAdvertiseListener advertiseListener;
    // 远程连接的设备
    private static BluetoothDevice bluetoothDevice;

    // 广播服务管理类
    public static BluetoothGattServer bluetoothGattServer;
    // 广播服务回调类
    private BluetoothGattServerCallback bluetoothGattServerCallback;
    // 广播服务监听类
    public static MyBluetoothGattServerListener bluetoothGattServerListener;
    // 广播服务类
    private BluetoothGattService bluetoothGattService;
    // 广播特征类
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    // 广播描述类
    private BluetoothGattDescriptor bluetoothGattDescriptor;
    // 广播服务列表
    private static List<BLEService> serviceList;

    static TextView characteristicValue, descriptorValue;

    private RecyclerView service_view;
    private static BLEServiceAdapter serviceAdapter;

    private int advertiseTimeout = 0 * 1000;
    private int REQUEST_BLUETOOTH_ENABLE = 200;
    private boolean isAdvertising = false, isOpen = false;

    public static MyHandler handler;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_advertise, container, false);
        context = getContext();
        initBLE();
        initView(root);
        handler = new MyHandler(getActivity());
        return root;
    }

    private void initView(View view) {
        advertise_state = (Switch) view.findViewById(R.id.advertise_state);
        connecttable = (Switch) view.findViewById(R.id.connecttable);
        advertise_btn = (Button) view.findViewById(R.id.advertise_btn);
        advertise_btn.setOnClickListener(this);

        connect_state = (Switch) view.findViewById(R.id.connect_state);
        server_btn = (Button) view.findViewById(R.id.server_btn);
        device_addr = (TextView) view.findViewById(R.id.device_addr);
        server_btn.setOnClickListener(this);

        add_service = (Button) view.findViewById(R.id.add_service);
        add_service.setOnClickListener(this);
        service_view = (RecyclerView) view.findViewById(R.id.sservice_view);
        serviceAdapter = new BLEServiceAdapter(context, serviceList, true);
        service_view.setLayoutManager(new LinearLayoutManager(context));
        service_view.setAdapter(serviceAdapter);

        initListener();
    }

    private void initBLE(){
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            assert bluetoothManager != null;
            bluetoothAdapter = bluetoothManager.getAdapter();
            advertiseCallback = new MyAdvertiseCallback();
            bluetoothGattServerCallback = new MyBluetoothGattServerCallback();
            serviceList = new ArrayList<>();
        } else {
            ToastUtils.showShort(context, "BLUETOOTH_LE is not supported");
        }
    }

    private void initListener() {
        advertiseListener = new MyAdvertiseListener() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                isAdvertising = true;
                advertise_btn.setText("STOP ADVERTISE");
                advertise_state.setChecked(true);
                if (settingsInEffect.isConnectable()) {
                    connecttable.setChecked(true);
                }
                // 定时关闭
                int timeout = settingsInEffect.getTimeout();
                if (timeout != 0) {
                    ToastUtils.showShort(context, "Turn off BLE in " + timeout/1000 + " seconds!");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            advertise_state.setChecked(false);
                            connecttable.setChecked(false);
                        }
                    }, timeout);
                }
            }

            @Override
            public void onStartFailure(int errorCode) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("onStartFailure : ");
                switch (errorCode) {
                    case ADVERTISE_FAILED_DATA_TOO_LARGE:
                        stringBuilder.append("ADVERTISE_FAILED_DATA_TOO_LARGE");
                        break;
                    case ADVERTISE_FAILED_ALREADY_STARTED:
                        stringBuilder.append("ADVERTISE_FAILED_ALREADY_STARTED");
                        break;
                    default:
                        stringBuilder.append("errorCode = " + errorCode);
                        break;
                }
                ToastUtils.showShort(context, stringBuilder.toString());
            }
        };

        bluetoothGattServerListener = new MyBluetoothGattServerListener() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                switch (status) {
                    case BluetoothGatt.GATT_SUCCESS:
                        switch (newState) {
                            case BluetoothProfile.STATE_DISCONNECTED:
                                connect_state.setChecked(false);
                                // TODO: 2020/6/3 Google bug，调用 cancelConnection 无法关闭
                                device_addr.setVisibility(View.GONE);
                                break;
                            case BluetoothProfile.STATE_CONNECTED:
                                connect_state.setChecked(true);
                                bluetoothDevice = device;
                                device_addr.setVisibility(View.VISIBLE);
                                device_addr.setText("Device Address : " + device.getAddress());
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        ToastUtils.showShort(context, "onConnectionStateChange GATT_FAILED!");
                        break;
                }
            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                switch (status) {
                    case GATT_SUCCESS:
                        serviceList.add(new BLEService(service));
                        serviceAdapter.notifyDataSetChanged();
                        break;
                    default:
                        ToastUtils.showShort(context, "onServiceAdded GATT_FAILED!");
                        break;
                }
            }

            /**
             * 客户端（连接方）请求读取特征时回调
             * @param device
             * @param requestId
             * @param offset
             * @param characteristic
             */
            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
            }

            /**
             * 客户端（连接方）修改特征时回调
             * @param device
             * @param requestId
             * @param characteristic
             * @param preparedWrite
             * @param responseNeeded
             * @param offset
             * @param value
             */
            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                characteristic.setValue(value);
                characteristicValue.setText("Value : " + new String(value));
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            }

            /**
             * 客户端（连接方）请求读取描述时回调
             * @param device
             * @param requestId
             * @param offset
             * @param descriptor
             */
            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, descriptor.getValue());
            }

            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                descriptor.setValue(value);
                descriptorValue.setText("Value : " + new String(value));
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            }

            @Override
            public void onNotificationSent(BluetoothDevice device, int status) {
                switch (status) {
                    case BluetoothGatt.GATT_SUCCESS:
                        break;
                    default:
                        ToastUtils.showShort(context, "onNotificationSent GATT_FAILED!");
                        break;
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.advertise_btn:
                if (isAdvertising) {
                    stopAdvertise();
                    isAdvertising = false;
                    advertise_state.setChecked(false);
                    connecttable.setChecked(false);
                    advertise_btn.setText("START ADVERTISE");
                } else {
                    startAdvertise();
                }
                break;
            case R.id.server_btn:
                if (isOpen) {
                    isOpen = false;
                    server_btn.setText("START GATT SERVER");
                    stopServer();
                } else {
                    isOpen = true;
                    server_btn.setText("STOP GATT SERVER");
                    startServer();
                }
                break;
            case R.id.add_service:
                addDemoService();
                break;
            default:
                break;
        }
    }

    /**
     * 发送广播
     */
    private void startAdvertise() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
            return;
        } else {
            bluetoothAdapter.setName("lalala");
            initAdvertiseSettings();
            initAdvertiseData();
            initAdvertiseResData();
            bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            if (bluetoothLeAdvertiser != null) {
                // 开始广播
                // 带响应报文
                bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertiseResData, advertiseCallback);
                // 不带响应报文
//                bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback);
            } else {
                ToastUtils.showShort(context, "get advertiser failed!");
            }
        }
    }

    /**
     * 广播设置
     */
    private void initAdvertiseSettings() {
        advertiseSettings = new AdvertiseSettings.Builder()
                // 广播模式，控制广播功率和延迟
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                // 广播发射功率级别
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                // 广播超时时间，最大值为 3*60*1000 毫秒，为 0 时禁用超时，默认无限广播
                .setTimeout(advertiseTimeout)
                // 广播连接类型
                .setConnectable(true)
                .build();
    }

    /**
     * 广播数据
     */
    private void initAdvertiseData() {
        advertiseData = new AdvertiseData.Builder()
                // 广播是否包含设备名称
                .setIncludeDeviceName(true)
                // 广播是否包含发射功率
                .setIncludeTxPowerLevel(true)
                // 添加服务uuid
                .addServiceUuid(new ParcelUuid(UUID.fromString(Container.TEST_UUID)))
                .build();
    }

    /**
     * 广播扫描的响应报文，optional
     */
    private void initAdvertiseResData() {
        advertiseResData = new AdvertiseData.Builder()
                // 添加自定义服务数据
                .addServiceData(new ParcelUuid(UUID.randomUUID()), new byte[]{1,2,3,4})
                // 添加自定义厂商数据
                .addManufacturerData(0x06, new byte[]{5,6,7,8})
                .build();
    }

    /**
     * 停止广播
     */
    private void stopAdvertise() {
        if (bluetoothLeAdvertiser != null) {
            bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
        }
    }

    /**
     * 启动 Gatt 服务
     */
    private void startServer() {
        bluetoothGattServer = bluetoothManager.openGattServer(context, bluetoothGattServerCallback);
    }

    /**
     * 清空Service并关闭服务
     */
    private void stopServer() {
        if (bluetoothGattServer != null) {
            bluetoothGattServer.clearServices();
            bluetoothGattServer.close();
        }
    }

    /**
     * 添加Gatt Service，也即广播数据
     */
    private void addDemoService() {
        if (bluetoothGattServer != null) {
            // 构造服务
            bluetoothGattService = new BluetoothGattService(UUID.randomUUID(), BluetoothGattService.SERVICE_TYPE_PRIMARY);

            // 构造特征
            bluetoothGattCharacteristic = new BluetoothGattCharacteristic(UUID.randomUUID(),
                    BluetoothGattCharacteristic.PROPERTY_READ |
                            BluetoothGattCharacteristic.PROPERTY_WRITE |
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_WRITE |
                            BluetoothGattCharacteristic.PERMISSION_READ);
            bluetoothGattCharacteristic.setValue("character_test_value");

            // 构造描述
            bluetoothGattDescriptor = new BluetoothGattDescriptor(UUID.randomUUID(),
                    BluetoothGattDescriptor.PERMISSION_READ |
                            BluetoothGattDescriptor.PERMISSION_WRITE);
            bluetoothGattDescriptor.setValue("descriptor_test_value".getBytes());

            // 添加描述
            bluetoothGattCharacteristic.addDescriptor(bluetoothGattDescriptor);
            // 添加特征
            bluetoothGattService.addCharacteristic(bluetoothGattCharacteristic);
            // 添加服务
            bluetoothGattServer.addService(bluetoothGattService);
        } else {
            ToastUtils.showShort(context, "GATT Server is NOT open!");
        }
    }

    /**
     * 移除GATT Service
     * @param service
     */
    public static void removeService(BluetoothGattService service) {
        if (bluetoothGattServer != null) {
            bluetoothGattServer.removeService(service);
            serviceAdapter.notifyDataSetChanged();
        } else {
            ToastUtils.showShort(context, "GATT Server is NOT open!");
        }
    }

    /**
     * 读取特征值
     * @param characteristic
     */
    public static void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        String valueStr = new String(characteristic.getValue());
        ToastUtils.showShort(context, "getValue = " + valueStr);
    }

    public static void setCharacteristicView(TextView value) {
        characteristicValue = value;
    }

    /**
     * 修改特征值
     * @param characteristic
     */
    public static void writeCharacteristic(final BluetoothGattCharacteristic characteristic) {
        final EditText input = new EditText(context);
        showInputDialog(input, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                characteristic.setValue(value.getBytes());
                characteristicValue.setText("Value : " + value);
                bluetoothGattServer.notifyCharacteristicChanged(bluetoothDevice, characteristic, false);
            }
        });
    }

    /**
     * 读取描述值
     * @param descriptor
     */
    public static void readDescriptor(BluetoothGattDescriptor descriptor) {
        String valueStr = new String(descriptor.getValue());
        ToastUtils.showShort(context, "getValue = " + valueStr);
    }

    public static void setDescriptorView(TextView value) {
        descriptorValue = value;
    }

    /**
     * 修改描述值
     * @param descriptor
     */
    public static void writeDescriptor(final BluetoothGattDescriptor descriptor) {
        final EditText input = new EditText(context);
        showInputDialog(input, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                descriptor.setValue(value.getBytes());
                descriptorValue.setText("Value : " + value);
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
            if (resultCode == Activity.RESULT_OK) {
                startAdvertise();
            } else {
                ToastUtils.showShort(context, "bluetooth is not enabled!");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(null);
        stopAdvertise();
        stopServer();
    }
}