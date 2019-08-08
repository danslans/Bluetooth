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

public class TaskSocket extends Thread {

    BluetoothAdapter bluetoothAdapter;
    Handler handler;
    boolean isConected = false;
    InputStream inputStream;
    OutputStream outputStream;
    BluetoothSocket bluetoothSocket;

    public TaskSocket(BluetoothAdapter bluetoothAdapter, Handler handler) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.handler = handler;
        try {
            BluetoothServerSocket bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.NAME, UUID.fromString(Constants.UUID));
            bluetoothSocket = bluetoothServerSocket.accept();
            if(bluetoothSocket.isConnected()){
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
                isConected = true;
            }else {
                try {
                    bluetoothSocket.close();
                } catch (IOException e) {
                    Log.d("Error cerrando conexion",e.getMessage());
                }
            }
        }catch (Exception e){
            Log.d("Error conectando",e.getMessage());
        }

    }

    @Override
    public void run() {
                        try {
                             byte[] datos = new byte[1024];
                             int datosLeidos ;
                             while (isConected){
                                  datosLeidos = inputStream.read(datos);
                                 handler.obtainMessage(Constants.MESSAGE_READ, datosLeidos, -1, datos)
                                         .sendToTarget();
                             }
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

        public void write(byte [] bytes) {
            try {
                outputStream.write(bytes);
                outputStream.flush();
            } catch (IOException e) {
                Log.d("Error-Writing",e.getMessage());
            }
    }

}
