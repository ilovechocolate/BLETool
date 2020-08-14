package com.nana.bletool.bean;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.ArrayList;
import java.util.List;

public class BLECharacteristic {

    public BLECharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
        this.uuid = characteristic.getUuid().toString();
        this.properties = characteristic.getProperties();
        this.value = characteristic.getValue();
        this.descriptors = new ArrayList<>();
        List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
        for (int i = 0; i < descriptorList.size(); i++) {
            this.descriptors.add(new BLEDescriptor(descriptorList.get(i)));
        }
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getProperties() {
        return properties;
    }

    public void setProperties(int properties) {
        this.properties = properties;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public List<BLEDescriptor> getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(List<BLEDescriptor> descriptors) {
        this.descriptors = descriptors;
    }

    public BLEDescriptor getDescriptor(String uuid) {
        for (int i = 0; i < descriptors.size(); i++) {
            BLEDescriptor descriptor = descriptors.get(i);
            if (descriptor.getUuid().equals(uuid)) {
                return descriptor;
            }
        }
        return null;
    }

    public void setDescriptor(BLEDescriptor descriptor) {
        for (int i = 0; i < descriptors.size(); i++) {
            if (descriptors.get(i).getUuid().equals(descriptor.uuid)) {
                descriptors.set(i, descriptor);
            }
        }
    }

    public int getDescriptorSize() {
        return descriptors.size();
    }

    BluetoothGattCharacteristic characteristic;
    String uuid;
    int properties;
    byte[] value;
    List<BLEDescriptor> descriptors;
}
