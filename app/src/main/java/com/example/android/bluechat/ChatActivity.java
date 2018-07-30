package com.example.android.bluechat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    BluetoothAdapter ba;
    ActionBar ab;
    ListView lv;
    ArrayList<String> al;
    ArrayAdapter<String> arr;
    EditText et;
    Handler h = new Handler();
    ReadWriteThread rwt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intn = getIntent();
        Bundle b = intn.getExtras();

        lv = (ListView) findViewById(R.id.msg_listView);
        et = (EditText) findViewById(R.id.msg_editText);
        al = new ArrayList<String>();
        arr = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, al);
        lv.setAdapter(arr);
        ba = BluetoothAdapter.getDefaultAdapter();
        String myName = ba.getName();
        ab = getSupportActionBar();
        ab.setTitle(myName);
        if (b == null) {
            ab.setSubtitle("Waiting for client...");
            ServerThread st = new ServerThread();
            st.start();
        } else {
            Object obj = b.get("Connect to");
            BluetoothDevice bd = (BluetoothDevice) obj;
            ab.setSubtitle(bd.getName());
            ClientThread ct = new ClientThread(bd);
            ct.start();
        }
    }

    public void sendMessage(View arg) {
        String s = et.getText().toString();
        rwt.writeData(s);
        al.add("Me: " + s);
        arr.notifyDataSetChanged();
        et.setText("");
    }

    public class ServerThread extends Thread {
        BluetoothServerSocket serverSocket;

        ServerThread() {
            try {
                serverSocket = ba.
                        listenUsingRfcommWithServiceRecord("server",
                                UUID.fromString(
                                        "00001101-0000-1000-8000-00805F9B34FB"));
            } catch (Exception e) {
                Log.d("Server Socket-->", e + "");
            }
        }

        @Override
        public void run() {
            try {
                BluetoothSocket client = serverSocket.accept();
                final BluetoothDevice bd = client.getRemoteDevice();
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        ab.setSubtitle(bd.getName());
                    }
                });

                rwt = new ReadWriteThread(client);
                rwt.start();
            } catch (Exception e) {
                Log.d("Server Socket-->", e + "");
            }
        }
    }

    public class ClientThread extends Thread {
        BluetoothSocket client;

        ClientThread(BluetoothDevice bd) {
            try {
                client = bd.createRfcommSocketToServiceRecord(UUID.fromString(
                        "00001101-0000-1000-8000-00805F9B34FB"));
            } catch (Exception e) {
                Log.d("Client Socket-->", e + "");
            }
        }

        @Override
        public void run() {
            try {
                client.connect();
                rwt = new ReadWriteThread(client);
                rwt.start();
            } catch (Exception e) {
                Log.d("Client Socket-->", e + "");
            }
        }
    }

    public class ReadWriteThread extends Thread {
        InputStream in;
        OutputStream out;

        ReadWriteThread(BluetoothSocket bs) {
            try {
                in = bs.getInputStream();
                out = bs.getOutputStream();
            } catch (Exception e) {
                Log.d("Read Write-->", e + "");
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    byte b[] = new byte[100];
                    in.read(b);
                    final String s = new String(b).trim();
                    if (s != null && !s.equals("")) {
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                al.add(s);
                                arr.notifyDataSetChanged();
                            }
                        });

                    }
                } catch (Exception e) {
                    Log.d("Read Write-->", e + "");
                }
            }
        }

        public void writeData(String s) {
            try {
                out.write(s.getBytes());
                out.flush();
            } catch (Exception e) {
                Log.d("Read Write-->", e + "");
            }
        }
    }
}
