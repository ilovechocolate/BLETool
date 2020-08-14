package com.nana.bletool.listener;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

public interface MyBluetoothGattServerListener {
    void onConnectionStateChange(final BluetoothDevice device, final int status, final int newState);
    void onServiceAdded(final int status, final BluetoothGattService service);
    void onCharacteristicReadRequest(final BluetoothDevice device, final int requestId, final int offset,
                                final BluetoothGattCharacteristic characteristic);
    void onCharacteristicWriteRequest(final BluetoothDevice device, final int requestId,
                                 final BluetoothGattCharacteristic characteristic,
                                 boolean preparedWrite, boolean responseNeeded,
                                 final int offset, final byte[] value);
    void onDescriptorReadRequest(final BluetoothDevice device, final int requestId,
                            final int offset, final BluetoothGattDescriptor descriptor);
    void onDescriptorWriteRequest(final BluetoothDevice device, final int requestId,
                                  final BluetoothGattDescriptor descriptor,
                                  boolean preparedWrite, boolean responseNeeded,
                                  final int offset, final byte[] value);
    void onNotificationSent(final BluetoothDevice device, final int status);


}
