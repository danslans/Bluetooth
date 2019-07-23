package com.example.meetbluetooth.task;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.meetbluetooth.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class TaskSocket {

    BluetoothAdapter bluetoothAdapter;
    Context context;
    int dato = 0;

    public TaskSocket(BluetoothAdapter bluetoothAdapter, Context context) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.context = context;
    }

    public void run() {
        try {
            BluetoothServerSocket bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.NAME, UUID.fromString(Constants.UUID));
            final BluetoothSocket bluetoothSocket = bluetoothServerSocket.accept();
            if(bluetoothSocket.isConnected()){
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            InputStream inputStream = bluetoothSocket.getInputStream();
                            OutputStream outputStream = bluetoothSocket.getOutputStream();
                            String saludo = "hola"+dato;
                            dato++;
                            outputStream.write(saludo.getBytes());
                            outputStream.flush();
                            Log.d("INFORMACION","Conectado");

                        }catch (Exception e){
                            Log.d("Error-Task",e.getMessage());
                            try {
                                bluetoothSocket.close();
                            } catch (IOException e1) {
                                Log.d("Error-Task",e.getMessage());
                            }
                        }
                    }
                },0,60000);
            }else {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
