package com.nana.bletool.bean;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;
import java.util.List;

public class BLEService {

    public BLEService(BluetoothGattService service) {
        this.service = service;
        this.uuid = service.getUuid().toString();
        this.type = service.getType();
        this.characteristics = new ArrayList<>();
        List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
        for (int i = 0; i < characteristicList.size(); i++) {
            this.characteristics.add(new BLECharacteristic(characteristicList.get(i)));
        }
    }

    public BluetoothGattService getService() {
        return service;
    }

    public void setService(BluetoothGattService service) {
        this.service = service;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<BLECharacteristic> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<BLECharacteristic> characteristics) {
        this.characteristics = characteristics;
    }

    public BLECharacteristic getCharacteristic(String uuid) {
        for (int i = 0; i < getCharacteristicSize(); i++) {
            BLECharacteristic characteristic = characteristics.get(i);
            if (characteristic.getUuid().equals(uuid)) {
                return characteristic;
            }
        }
        return null;
    }

    public void setCharacteristic(BLECharacteristic characteristic) {
        for (int i = 0; i < getCharacteristicSize(); i++) {
            if (characteristics.get(i).getUuid().equals(characteristic.uuid)) {
                characteristics.set(i, characteristic);
            }
        }
    }

    public int getCharacteristicSize() {
        return characteristics.size();
    }

    BluetoothGattService service;
    String uuid;
    int type;
    List<BLECharacteristic> characteristics;
}
