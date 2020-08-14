package com.nana.bletool.adapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.nana.bletool.BLEConnectionActivity;
import com.nana.bletool.BLEUtils;
import com.nana.bletool.R;
import com.nana.bletool.bean.BLEData;
import com.nana.bletool.bean.BLEDevice;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class BLEDeviceAdapter extends RecyclerView.Adapter<BLEDeviceAdapter.BLEDeviceViewHolder> {

    private Context context;
    private List<BLEDevice> bleDeviceList;
    private List<BLEData> bleDataList;

    public BLEDeviceAdapter(Context context, List<BLEDevice> bleDeviceList) {
        this.context = context;
        this.bleDeviceList = bleDeviceList;
        this.bleDataList = new ArrayList<>();
    }

    @NonNull
    @Override
    public BLEDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BLEDeviceViewHolder(LayoutInflater.from(context).inflate(R.layout.bledevice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BLEDeviceViewHolder holder, int position) {
        holder.bindDevice(bleDeviceList.get(position));
    }

    @Override
    public int getItemCount() {
        return bleDeviceList.size();
    }

    class BLEDeviceViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout device_layout;
        TextView device_name, address, state, brand, rssi;
        Button connect;
        LinearLayout data_layout;
        TextView service_data;
        TableLayout table_layout;

        boolean isFlatting = false;

        private BLEDeviceViewHolder(View view) {
            super(view);
            initView(view);
        }

        private void initView(View view) {
            device_layout = (ConstraintLayout) view.findViewById(R.id.device_layout);
            device_name = (TextView) view.findViewById(R.id.device_name);
            address = (TextView) view.findViewById(R.id.address);
            state = (TextView) view.findViewById(R.id.state);
            brand = (TextView) view.findViewById(R.id.brand);
            rssi = (TextView) view.findViewById(R.id.rssi);
            connect = (Button) view.findViewById(R.id.connect);

            data_layout = (LinearLayout) view.findViewById(R.id.data_layout);
            service_data = (TextView) view.findViewById(R.id.service_data);
            table_layout = (TableLayout) view.findViewById(R.id.table_layout);

            data_layout.setVisibility(View.GONE);
            table_layout.setVisibility(View.GONE);
        }

        private void bindDevice(BLEDevice bleDevice) {
            String deviceName = bleDevice.device.getName();
            device_name.setText(deviceName == null ? "Unknown" : deviceName);
            String deviceAddr = bleDevice.device.getAddress();
            address.setText(deviceAddr);
            int bondState = bleDevice.device.getBondState();
            switch (bondState) {
                // 12，已绑定
                case BluetoothDevice.BOND_BONDED:
                    state.setText("BONDED");
                    break;
                // 11，绑定中
                case BluetoothDevice.BOND_BONDING:
                    state.setText("BONDING");
                    break;
                // 10，未绑定
                case BluetoothDevice.BOND_NONE:
                    state.setText("NONE");
                    break;
                default:
                    state.setText("UNKNOWN");
                    break;
            }
            if (bleDevice.scanRecord.getManufacturerSpecificData(0x4C) != null) {
                brand.setText("Brand : Apple");
            } else if (bleDevice.scanRecord.getManufacturerSpecificData(0xE0) != null) {
                brand.setText("Brand : Google");
            } else if (bleDevice.scanRecord.getManufacturerSpecificData(0x06) != null) {
                brand.setText("Brand : Microsoft");
            } else {
                brand.setText("Brand : Unknown");
            }
            rssi.setText("RSSI : " + bleDevice.rssi);

            initListener(bleDevice.device.getAddress(), bleDevice.scanRecord);
        }

        private void initListener(final String address, final ScanRecord scanRecord) {
            final String scanRes = BLEUtils.byte2HexString(scanRecord.getBytes());
            device_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isFlatting) {
                        data_layout.setVisibility(View.GONE);
                        table_layout.removeAllViews();
                        table_layout.setVisibility(View.GONE);
                        isFlatting = false;
                    } else {
                        data_layout.setVisibility(View.VISIBLE);
                        table_layout.setVisibility(View.VISIBLE);
                        service_data.setText(scanRes);
                        bleDataList.clear();
                        bleDataList = BLEUtils.parseBleData(scanRes);
                        setDataTable();
                        isFlatting = true;
                    }
                }
            });
            connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, BLEConnectionActivity.class);
                    intent.putExtra("data", scanRes);
                    intent.putExtra("address", address);
                    context.startActivity(intent);
                }
            });
        }

        // 格式化显示数据包
        private void setDataTable() {
            // 添加表头
            TableRow tableRow = new TableRow(context);
            TextView row_len = new TextView(context);
            row_len.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            row_len.setPadding(3,3,3,3);
            row_len.setGravity(Gravity.CENTER);
            row_len.setText("Length");
            tableRow.addView(row_len);
            TextView row_type = new TextView(context);
            row_type.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            row_type.setPadding(3,3,3,3);
            row_type.setGravity(Gravity.CENTER);
            row_type.setText("Type");
            tableRow.addView(row_type);
            TextView row_data = new TextView(context);
            row_data.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3));
            row_data.setPadding(3,3,3,3);
            row_data.setGravity(Gravity.CENTER);
            row_data.setText("Data");
            tableRow.addView(row_data);
            tableRow.setBackgroundColor(context.getResources().getColor(R.color.dark_gray));
            table_layout.addView(tableRow);

            // 添加表数据
            for (BLEData bleData : bleDataList) {
                tableRow = new TableRow(context);
                row_len = new TextView(context);
                row_len.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                row_len.setPadding(3,3,3,3);
                row_len.setGravity(Gravity.CENTER);
                row_len.setText(String.valueOf(bleData.length));
                tableRow.addView(row_len);
                row_type = new TextView(context);
                row_type.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                row_type.setPadding(3,3,3,3);
                row_type.setGravity(Gravity.CENTER);
                row_type.setText("0x" + bleData.type);
                tableRow.addView(row_type);
                row_data = new TextView(context);
                row_data.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3));
                row_data.setPadding(3,3,3,3);
                row_data.setGravity(Gravity.CENTER);
                row_data.setText("0x" + bleData.data);
                tableRow.addView(row_data);
                tableRow.setBackgroundColor(context.getResources().getColor(R.color.gray));
                table_layout.addView(tableRow);
                ImageView line = new ImageView(context);
                line.setBackgroundColor(context.getResources().getColor(R.color.dark_gray));
                table_layout.addView(new ImageView(context), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            }
        }
    }
}