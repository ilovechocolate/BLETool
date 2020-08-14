package com.nana.bletool.listener;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

public interface MyBluetoothGattListener {
    void onConnectionStateChange(int status, int newState);
    void onServicesDiscovered(int status);
    void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status);
    void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status);
    void onCharacteristicChanged(BluetoothGattCharacteristic characteristic);
    void onDescriptorRead(BluetoothGattDescriptor descriptor, int status);
    void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status);
}
