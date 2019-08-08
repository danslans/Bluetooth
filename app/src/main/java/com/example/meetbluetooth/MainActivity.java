package com.example.meetbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;

import com.example.meetbluetooth.adapter.ChatAdapter;
import com.example.meetbluetooth.adapter.DeviceAdapter;
import com.example.meetbluetooth.task.TaskSocket;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    RecyclerView lista,chatlist;
    DeviceAdapter deviceAdapter;
    BluetoothAdapter bluetoothAdapter;
    ChatAdapter chatAdapter;
    EditText editText;
    Button enviar;
    ArrayList<String> msnChat =new ArrayList<>();
    Handler handler;
    StringBuffer stringBuffer = new StringBuffer();
    TaskSocket taskSocket;
    boolean isServer = false;
    ThreadDevice threadDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent,0);

        Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();

        lista = findViewById(R.id.lista);
        lista.setHasFixedSize(true);
        lista.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        chatlist = findViewById(R.id.listChat);
        chatlist.setHasFixedSize(true);
        chatlist.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        editText =  findViewById(R.id.etxtTexto);
        enviar =  findViewById(R.id.btnEnviar);
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isServer) {
                    handler.obtainMessage(Constants.MESSAGE_WRITE, editText.getText().toString().getBytes()).sendToTarget();
                }else{
                    handler.obtainMessage(Constants.MESSAGE_WRITE_CLIENT, editText.getText().toString().getBytes()).sendToTarget();
                }
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case Constants.MESSAGE_READ:
                        try {
                            byte[] bytes =(byte[]) msg.obj;
                            String data = new String(bytes) ;
                            msnChat.add(data);
                            chatAdapter.notifyDataSetChanged();
                        } catch (Exception e){
                            Log.e ("Error Handler",e.getMessage());
                        }

                        break;
                    case  Constants.MESSAGE_WRITE:
                        byte[] bytes =(byte[]) msg.obj;
                        msnChat.add("yo: "+new String(bytes));
                        chatAdapter.notifyDataSetChanged();
                        taskSocket.write(bytes);
                        break;
                    case  Constants.MESSAGE_WRITE_CLIENT:
                        try {
                            byte[] datos =(byte[]) msg.obj;
                            msnChat.add("yo: "+new String(datos));
                            chatAdapter.notifyDataSetChanged();
                            threadDevice.write(datos);
                        }catch (Exception e){
                            Log.e ("Error Handler",e.getMessage());
                        }
                        break;
                    case  Constants.MESSAGE_CONNECTED:
                        threadDevice = new ThreadDevice((BluetoothSocket) msg.obj);
                        threadDevice.start();
                        break;
                }
            }
        };

        ArrayList<BluetoothDevice> bluetoothDeviceArrayList = new ArrayList<>();
        for (BluetoothDevice bluetoothDevice : bluetoothDevices)
        {
            bluetoothDeviceArrayList.add(bluetoothDevice);
        }

        deviceAdapter = new DeviceAdapter(getApplicationContext(),bluetoothDeviceArrayList,handler);
        lista.setAdapter(deviceAdapter);
        msnChat.add("texto prueba");
        chatAdapter = new ChatAdapter(msnChat);
        chatlist.setAdapter(chatAdapter);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            taskSocket = new TaskSocket(bluetoothAdapter,handler);
            taskSocket.start();
            isServer = true;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

 /*   public static void main(String... arg){
        try {
            InputStream inputStream = new ByteArrayInputStream("hola1".getBytes());
            StringBuilder stringBuilder = new StringBuilder();
            int i;
            while ((i = inputStream.read())>0){
                char letra = (char) i;
                stringBuilder.append(letra);
            }
            System.out.println(stringBuilder.toString());
        }catch (Exception e){
            System.out.println(e);
        }

    }*/


    private class ThreadDevice extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream ;
        private final OutputStream outputStream ;
        int mState;
        private ThreadDevice(BluetoothSocket bluetoothSocket) {
            this.bluetoothSocket = bluetoothSocket;
            InputStream inStream=null;
            OutputStream oStream =null;

            try {
                inStream = bluetoothSocket.getInputStream();
                oStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = inStream;
            outputStream = oStream;
            mState = 1;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mState == 1) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    System.out.println(e);
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);

                // Share the sent message back to the UI Activity
                //handler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                //        .sendToTarget();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
