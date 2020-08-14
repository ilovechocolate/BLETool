package com.nana.bletool.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;

import static com.nana.bletool.ui.AdvertiseFragment.bluetoothGattServerListener;
import static com.nana.bletool.ui.AdvertiseFragment.handler;

/**
 * 广播服务连接回调
 */
public class MyBluetoothGattServerCallback extends BluetoothGattServerCallback {

    /**
     * 设备状态改变回调，连接或断开
     * @param device
     * @param status
     * @param newState
     */
    @Override
    public void onConnectionStateChange(final BluetoothDevice device, final int status, final int newState) {
        super.onConnectionStateChange(device, status, newState);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattServerListener.onConnectionStateChange(device, status, newState);
            }
        });
    }

    /**
     * 添加本地服务
     * @param status
     * @param service
     */
    @Override
    public void onServiceAdded(final int status, final BluetoothGattService service) {
        super.onServiceAdded(status, service);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattServerListener.onServiceAdded(status, service);
            }
        });
    }

    /**
     * 读取特征值
     * @param device
     * @param requestId
     * @param offset
     * @param characteristic
     */
    @Override
    public void onCharacteristicReadRequest(final BluetoothDevice device, final int requestId, final int offset,
                                            final BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattServerListener.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            }
        });
  }

    /**
     * 写入特征值
     * @param device
     * @param requestId
     * @param characteristic
     * @param preparedWrite
     * @param responseNeeded
     * @param offset
     * @param value
     */
    @Override
    public void onCharacteristicWriteRequest(final BluetoothDevice device, final int requestId,
                                             final BluetoothGattCharacteristic characteristic,
                                             final boolean preparedWrite, final boolean responseNeeded,
                                             final int offset, final byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                responseNeeded, offset, value);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattServerListener.onCharacteristicWriteRequest(device, requestId,
                        characteristic, preparedWrite, responseNeeded, offset, value);
            }
        });
    }

    /**
     * 读取描述值
     * @param device
     * @param requestId
     * @param offset
     * @param descriptor
     */
    @Override
    public void onDescriptorReadRequest(final BluetoothDevice device, final int requestId,
                                        final int offset, final BluetoothGattDescriptor descriptor) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattServerListener.onDescriptorReadRequest(device, requestId, offset, descriptor);
            }
        });
    }

    /**
     * 写入描述值
     * @param device
     * @param requestId
     * @param descriptor
     * @param preparedWrite
     * @param responseNeeded
     * @param offset
     * @param value
     */
    @Override
    public void onDescriptorWriteRequest(final BluetoothDevice device, final int requestId,
                                         final BluetoothGattDescriptor descriptor,
                                         final boolean preparedWrite, final boolean responseNeeded,
                                         final int offset, final byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattServerListener.onDescriptorWriteRequest(device, requestId, descriptor,
                        preparedWrite, responseNeeded, offset, value);
            }
        });
    }


    @Override
    public void onNotificationSent(final BluetoothDevice device, final int status) {
        super.onNotificationSent(device, status);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattServerListener.onNotificationSent(device, status);
            }
        });
    }
}