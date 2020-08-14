package com.nana.bletool.bean;

import android.bluetooth.BluetoothGattDescriptor;

public class BLEDescriptor {

    public BLEDescriptor(BluetoothGattDescriptor descriptor) {
        this.descriptor = descriptor;
        this.uuid = descriptor.getUuid().toString();
        this.value = descriptor.getValue();
    }

    public BluetoothGattDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(BluetoothGattDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    BluetoothGattDescriptor descriptor;
    String uuid;
    byte[] value;
}
