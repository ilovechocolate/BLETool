package com.nana.bletool.adapter;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nana.bletool.BLEConnectionActivity;
import com.nana.bletool.BLEUtils;
import com.nana.bletool.Container;
import com.nana.bletool.R;
import com.nana.bletool.ToastUtils;
import com.nana.bletool.bean.BLECharacteristic;
import com.nana.bletool.bean.BLEDescriptor;
import com.nana.bletool.bean.BLEService;
import com.nana.bletool.ui.AdvertiseFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class BLEServiceAdapter extends RecyclerView.Adapter<BLEServiceAdapter.BLEServiceViewHolder> {

    private Context context;
    private List<BLEService> serviceList;
    private boolean isServer;

    public BLEServiceAdapter(Context context, List<BLEService> serviceList, boolean isServer) {
        this.context = context;
        this.serviceList = serviceList;
        this.isServer = isServer;
    }

    @NonNull
    @Override
    public BLEServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BLEServiceViewHolder(LayoutInflater.from(context).inflate(R.layout.bleservice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BLEServiceViewHolder holder, int position) {
        holder.bindService(serviceList.get(position));
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    class BLEServiceViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout service_layout;
        LinearLayout character_container;
        TextView service_proto, service_uuid, service_type;
        ImageView service_line, service_more;

        boolean isChaFlatting = false, isDesFaltting = false;

        private BLEServiceViewHolder(View view) {
            super(view);
            initServiceView(view);
        }

        private void initServiceView(View view) {
            service_layout = view.findViewById(R.id.service_layout);
            service_proto = view.findViewById(R.id.service_proto);
            service_uuid = view.findViewById(R.id.service_uuid);
            service_type = view.findViewById(R.id.service_type);
            service_more = view.findViewById(R.id.service_more);
            service_line = view.findViewById(R.id.service_line);
            character_container = view.findViewById(R.id.character_container);
        }

        private void bindService(BLEService service) {
            String uuid = service.getUuid();
            service_uuid.setText(uuid);
            if (isServer) {
                service_proto.setVisibility(View.GONE);
            } else {
                service_proto.setText("PROTO : ");
                String proto = BLEUtils.getProtocol(uuid);
                if (proto.equals("UNKNOWN")) {
                    service_proto.setVisibility(View.GONE);
                } else {
                    service_proto.append(proto);
                }
            }
            service_type.setText("Type : ");
            service_type.append(BLEUtils.getServiceType(service.getType()));

            initServiceListener(service);
        }

        private void initServiceListener(final BLEService service) {
            service_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isChaFlatting) {
                        service_line.setVisibility(View.VISIBLE);
                        character_container.removeAllViews();
                        character_container.setVisibility(View.GONE);
                        isChaFlatting = false;
                        service_more.setBackground(context.getResources().getDrawable(R.drawable.ic_less));
                    } else {
                        if (service.getCharacteristicSize() != 0) {
                            service_line.setVisibility(View.GONE);
                            character_container.setVisibility(View.VISIBLE);
                            for (BLECharacteristic characteristic : service.getCharacteristics()) {
                                initCharacteristicView(characteristic);
                            }
                        }
                        isChaFlatting = true;
                        service_more.setBackground(context.getResources().getDrawable(R.drawable.ic_more));
                    }
                }
            });
            if (isServer) {
                service_layout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // TODO: 2020/6/4 待实现更好的用户交互
                        AdvertiseFragment.removeService(service.getService());
                        service_layout.setVisibility(View.GONE);
                        service_line.setVisibility(View.GONE);
                        character_container.setVisibility(View.GONE);
                        ToastUtils.showShort(context, "service is removed!");
                        return true;
                    }
                });
            }
        }

        private void initCharacteristicView(BLECharacteristic characteristic) {
            LinearLayout character_layout, descriptor_container;
            TextView character_uuid, character_prop, character_value;
            ImageView character_line, character_more;
            Button character_read, character_write;

            View view = LayoutInflater.from(context).inflate(R.layout.blecharacter, character_container, false);
            character_layout = view.findViewById(R.id.character_layout);
            character_uuid = view.findViewById(R.id.character_uuid);
            character_prop = view.findViewById(R.id.character_prop);
            character_value = view.findViewById(R.id.character_value);
            character_read = view.findViewById(R.id.character_read);
            character_write = view.findViewById(R.id.character_write);
            character_more = view.findViewById(R.id.character_more);
            character_line = view.findViewById(R.id.character_line);
            descriptor_container = view.findViewById(R.id.descriptor_container);

            bindCharacteristic(characteristic, character_uuid, character_prop, character_value);
            character_container.addView(character_layout);

            AdvertiseFragment.setCharacteristicView(character_value);

            initCharacteristicListener(characteristic, character_read, character_write,
                    character_value, character_layout, descriptor_container, character_line,
                    character_more);
        }

        private void bindCharacteristic(BLECharacteristic characteristic,
                                        TextView uuid, TextView prop, TextView value) {
            uuid.setText(characteristic.getUuid());
            prop.setText("Prop : ");
            String propertiesStr = BLEUtils.getProperties(characteristic.getProperties());
            prop.append(propertiesStr.isEmpty() ? "NULL" : propertiesStr);
            byte[] value_bytes = characteristic.getValue();
            if (value_bytes == null) {
                value.setText("Value : NULL");
            } else {
                value.setText("Value : " + new String(value_bytes));
            }
        }

        private void initCharacteristicListener(final BLECharacteristic characteristic,
                                                Button read, final Button write, final TextView value,
                                                LinearLayout layout, final LinearLayout container,
                                                final ImageView line, final ImageView more) {
            read.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isServer) {
                        AdvertiseFragment.readCharacteristic(characteristic.getCharacteristic());
                    } else {
                        BLEConnectionActivity.readCharacteristic(characteristic.getCharacteristic(), value);
                    }
                }
            });
            write.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isServer) {
                        AdvertiseFragment.writeCharacteristic(characteristic.getCharacteristic());
                    } else {
                        BLEConnectionActivity.writeCharacteristic(characteristic.getCharacteristic(), value);
                    }
                }
            });
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isDesFaltting) {
                        line.setVisibility(View.VISIBLE);
                        container.removeAllViews();
                        container.setVisibility(View.GONE);
                        isDesFaltting = false;
                        more.setBackground(context.getResources().getDrawable(R.drawable.ic_less));
                    } else {
                        if (characteristic.getDescriptorSize() != 0) {
                            line.setVisibility(View.GONE);
                            container.setVisibility(View.VISIBLE);
                            for (BLEDescriptor descriptor : characteristic.getDescriptors()) {
                                initDescriptorView(descriptor, container);
                            }
                        }
                        isDesFaltting = true;
                        more.setBackground(context.getResources().getDrawable(R.drawable.ic_more));
                    }
                }
            });
        }

        private void initDescriptorView(BLEDescriptor descriptor, LinearLayout container) {
            ConstraintLayout descriptor_layout;
            TextView descriptor_uuid, descriptor_value;
            Button descriptor_read, descriptor_write;

            View view = LayoutInflater.from(context).inflate(R.layout.bledescriptor, container, false);
            descriptor_layout = view.findViewById(R.id.descriptor_layout);
            descriptor_uuid = view.findViewById(R.id.descriptor_uuid);
            descriptor_value = view.findViewById(R.id.descriptor_value);
            descriptor_read = view.findViewById(R.id.descriptor_read);
            descriptor_write = view.findViewById(R.id.descriptor_write);

            bindDescriptor(descriptor, descriptor_uuid, descriptor_value);
            container.addView(descriptor_layout);

            AdvertiseFragment.setDescriptorView(descriptor_value);

            initDescriptorListener(descriptor.getDescriptor(), descriptor_read, descriptor_write, descriptor_value);
        }

        private void bindDescriptor(BLEDescriptor descriptor, TextView uuid, TextView value) {
            uuid.setText(descriptor.getUuid());
            byte[] value_bytes = descriptor.getValue();
            if (value_bytes == null) {
                value.setText("Value : NULL");
            } else {
                value.setText("Value : " + new String(value_bytes));
            }
        }

        private void initDescriptorListener(final BluetoothGattDescriptor descriptor,
                                            Button read, final Button write, final TextView value) {
            read.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isServer) {
                        AdvertiseFragment.readDescriptor(descriptor);
                    } else {
                        BLEConnectionActivity.readDescriptor(descriptor, value);
                    }
                }
            });
            write.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isServer) {
                        AdvertiseFragment.writeDescriptor(descriptor);
                    } else {
                        BLEConnectionActivity.writeDescriptor(descriptor, value);
                    }
                }
            });
        }

        private void print(BluetoothGattService service) {
            Log.d(Container.TAG, "service id = " + service.getUuid().toString());
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                Log.d(Container.TAG, "characteristic id = " + characteristic.getUuid().toString());
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    Log.d(Container.TAG, "descriptor id = " + descriptor.getUuid().toString());
                }
            }
        }
    }
}