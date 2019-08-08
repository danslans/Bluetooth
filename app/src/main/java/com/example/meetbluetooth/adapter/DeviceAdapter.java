package com.example.meetbluetooth.adapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetbluetooth.Constants;
import com.example.meetbluetooth.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.Holder> {
    private Context context;
    private ArrayList<BluetoothDevice> strings;
    private Handler handler;

    public DeviceAdapter(Context context, ArrayList<BluetoothDevice> strings, Handler handler) {
        this.context = context;
        this.strings = strings;
        this.handler = handler;
    }

    @NonNull
    @Override
    public DeviceAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapter.Holder holder, int position) {
        final BluetoothDevice bluetoothDevice = strings.get(position);
        holder.textView.setText(bluetoothDevice.getName());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ParcelUuid[] uuids = bluetoothDevice.getUuids();
                    final BluetoothSocket bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(Constants.UUID));
                    bluetoothSocket.connect();
                    if(bluetoothSocket.isConnected()){
                        handler.obtainMessage(Constants.MESSAGE_CONNECTED,bluetoothSocket).sendToTarget();
                    }else{
                        handler.obtainMessage(Constants.MESSAGE_CONNECTED,false).sendToTarget();
                        bluetoothSocket.close();
                    }
                } catch (Exception e){
                    Log.e("Error-general",e.getMessage());
                }




            }
        });
    }

    @Override
    public int getItemCount() {
        return strings.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView textView;
        LinearLayout linearLayout;
        public Holder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            linearLayout = itemView.findViewById(R.id.card);
        }
    }

}
