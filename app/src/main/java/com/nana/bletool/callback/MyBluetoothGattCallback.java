package com.nana.bletool.callback;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import static com.nana.bletool.BLEConnectionActivity.handler;
import static com.nana.bletool.BLEConnectionActivity.bluetoothGattListener;

/**
 * 蓝牙通信连接回调
 */
public class MyBluetoothGattCallback extends BluetoothGattCallback {

    /**
     * 连接（发起方）状态改变回调
     *
     * @param gatt
     * @param status
     * @param newState
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, final int status, final int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattListener.onConnectionStateChange(status, newState);
            }
        });
    }

    /**
     * 发现服务
     *
     * @param gatt
     * @param status
     */
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, final int status) {
        super.onServicesDiscovered(gatt, status);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattListener.onServicesDiscovered(status);
            }
        });
    }

    /**
     * 特征改变
     *
     * @param gatt
     * @param characteristic
     */
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattListener.onCharacteristicChanged(characteristic);
            }
        });
    }

    /**
     * 读取特征
     *
     * @param gatt
     * @param characteristic
     * @param status
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattListener.onCharacteristicRead(characteristic, status);
            }
        });
    }

    /**
     * 修改特征
     *
     * @param gatt
     * @param characteristic
     * @param status
     */
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattListener.onCharacteristicWrite(characteristic, status);
            }
        });
    }

    /**
     * 读取描述
     *
     * @param gatt
     * @param descriptor
     * @param status
     */
    @Override
    public void onDescriptorRead(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattListener.onDescriptorRead(descriptor, status);
            }
        });
    }

    /**
     * 修改描述
     *
     * @param gatt
     * @param descriptor
     * @param status
     */
    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        handler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothGattListener.onDescriptorWrite(descriptor, status);
            }
        });
    }
}