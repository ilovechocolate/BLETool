package com.nana.bletool;

import com.nana.bletool.bean.BLEData;

import java.util.ArrayList;
import java.util.List;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_BROADCAST;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
import static android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY;
import static android.bluetooth.BluetoothGattService.SERVICE_TYPE_SECONDARY;

public class BLEUtils {

    public static String byte2HexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (bytes != null && bytes.length != 0) {
            for (int i = 0; i < bytes.length; i++) {
                String tmp = Integer.toHexString(bytes[i] & 0XFF);
                if (tmp.length() < 2) {
                    stringBuilder.append("0");
                }
                stringBuilder.append(tmp);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 解析蓝牙数据包，格式为｜ len | type | data |
     *
     * @param scanRecord
     */
    public static List<BLEData> parseBleData(String scanRecord) {
        List<BLEData> bleDataList = new ArrayList<>();
        while (true) {
            String length = scanRecord.substring(0, 2);
            int len = Integer.parseInt(length, 16);
            if (len == 0) {
                break;
            }
            String type = scanRecord.substring(2, 4);
            String data = scanRecord.substring(4, 2 + len * 2);
            scanRecord = scanRecord.substring(2 + len * 2);
            bleDataList.add(new BLEData(len, type, data));
        }
        return bleDataList;
    }

    /**
     * 获取服务的协议类型
     *
     * @param uuid
     * @return
     */
    public static String getProtocol(String uuid) {
        switch (uuid.toUpperCase()) {
            case Container.GAP_UUID:
                return "GAP";
            case Container.GATT_UUID:
                return "GATT";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * 获取服务的类型
     *
     * @param type
     * @return
     */
    public static String getServiceType(int type) {
        switch (type) {
            case SERVICE_TYPE_PRIMARY:
                return "PRIMARY";
            case SERVICE_TYPE_SECONDARY:
                return "SECONDARY";
            default:
                return "UNKNOWN";
        }
    }

    public static String getProperties(int properties) {
        StringBuilder stringBuilder = new StringBuilder();
        if ((properties & PROPERTY_BROADCAST) != 0) {
            stringBuilder.append("PROPERTY_BROADCAST | ");
        }
        if ((properties & PROPERTY_READ) != 0) {
            stringBuilder.append("PROPERTY_READ | ");
        }
        if ((properties & PROPERTY_WRITE_NO_RESPONSE) != 0) {
            stringBuilder.append("PROPERTY_WRITE_NO_RESPONSE | ");
        }
        if ((properties & PROPERTY_WRITE) != 0) {
            stringBuilder.append("PROPERTY_WRITE | ");
        }
        if ((properties & PROPERTY_NOTIFY) != 0) {
            stringBuilder.append("PROPERTY_NOTIFY | ");
        }
        if ((properties & PROPERTY_INDICATE) != 0) {
            stringBuilder.append("PROPERTY_INDICATE | ");
        }
        if ((properties & PROPERTY_SIGNED_WRITE) != 0) {
            stringBuilder.append("PROPERTY_SIGNED_WRITE | ");
        }
        if ((properties & PROPERTY_EXTENDED_PROPS) != 0) {
            stringBuilder.append("PROPERTY_EXTENDED_PROPS | ");
        }
        String propertiesValue = stringBuilder.toString();
        if (propertiesValue.length() > 0) {
            return propertiesValue.substring(0, propertiesValue.length() - 3);
        } else {
            return "";
        }
    }
}