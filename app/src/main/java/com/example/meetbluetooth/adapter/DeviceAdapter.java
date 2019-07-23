package com.example.meetbluetooth.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
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
                    Toast.makeText(context, uuids[0].getUuid().toString(), Toast.LENGTH_SHORT).show();
                    bluetoothSocket.connect();
                    if(bluetoothSocket.isConnected()){
                        Timer timer = new Timer();
                        timer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    InputStream inputStream = bluetoothSocket.getInputStream();
                                    OutputStream outputStream = bluetoothSocket.getOutputStream();

                                    Message msn = new Message();
                                    msn.obj = inputStream;
                                    msn.what = 1;
                                    handler.sendMessage(msn);
                                    Log.e ("Darkcode","Se recibe");
                                }catch (Exception e){
                                    Log.e("ERROR",e.getMessage());
                                    try {
                                        bluetoothSocket.close();
                                    } catch (IOException e1) {
                                        Log.e("ERROR",e.getMessage());
                                    }
                                }
                            }
                        },0,60000);

                    }else{
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
