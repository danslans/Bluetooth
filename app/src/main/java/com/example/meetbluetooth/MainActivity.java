package com.example.meetbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    RecyclerView lista,chatlist;
    DeviceAdapter deviceAdapter;
    BluetoothAdapter bluetoothAdapter;
    EditText editText;
    Button enviar;
    ArrayList<String> msnChat =new ArrayList<>();
    Handler handler;

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

            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1:
                        try {
                            int i;
                            char c;
                            StringBuilder stringBuilder = new StringBuilder();
                            InputStream inputStream =(InputStream) msg.obj;
                            while((i = inputStream.read())!=-1) {
                                c = (char) i;
                                stringBuilder.append(c);
                                msnChat.add(stringBuilder.toString());
                                deviceAdapter.notifyDataSetChanged();
                                Log.e ("Resultado",stringBuilder.toString());
                            }
                            Log.e ("Resultado",stringBuilder.toString());
                        } catch (Exception e){
                            Log.e ("Error Handler",e.getMessage());
                        }

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
        ChatAdapter chatAdapter = new ChatAdapter(msnChat);
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
            TaskSocket taskSocket = new TaskSocket(bluetoothAdapter,getApplicationContext());
            taskSocket.run();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
