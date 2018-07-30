package com.example.android.bluechat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_PRIVILEGED;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    BluetoothAdapter ba;
    Button b1;
    Button b2;
    //    ArrayList<String> al = new ArrayList<>();
//    ArrayList<String> al2 = new ArrayList<>();
//    ArrayAdapter<String> arrAd;
    ArrayList<String> pal = new ArrayList<String>();
    ArrayList<String> pal2 = new ArrayList<String>();
    ArrayList<BluetoothDevice> pal3 = new ArrayList<>();
    ArrayAdapter<String> arrAd2;

    boolean flag;
    BroadcastReceiver br1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            BluetoothDevice bd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            if(!al2.contains(bd.getAddress())){
//                al.add(bd.getName());
//                al2.add(bd.getAddress());
//                arrAd.notifyDataSetChanged();
//            }else if (al.contains(bd.getName())){
//                al.remove(bd.getName());
//                al.add(0,bd.getName());
//            }
        }
    };
    BroadcastReceiver br2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(br1);
            unregisterReceiver(br2);

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isPermissionGiven()) {
            b1 = findViewById(R.id.button1);
            b2 = findViewById(R.id.button2);

            ba = BluetoothAdapter.getDefaultAdapter();
//        ListView lv = findViewById(R.id.listView1);
//        arrAd = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item,al);
//        lv.setAdapter(arrAd);

            ListView lv2 = findViewById(R.id.listView2);
            arrAd2 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, pal);
            lv2.setAdapter(arrAd2);

            if (ba.isEnabled()) {
                b1.setText("Turn Off Bluetooth");
                b2.setEnabled(true);
            } else {
                b2.setEnabled(false);
            }
            lv2.setOnItemClickListener(this);
            registerReceiver(br1, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            registerReceiver(br2, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        }
    }

    public void getPairedDevices() {
        Set<BluetoothDevice> devices = ba.getBondedDevices();
        Iterator<BluetoothDevice> itr = devices.iterator();
        while (itr.hasNext()) {
            BluetoothDevice bd = itr.next();
            pal.add(bd.getName());
            pal2.add(bd.getAddress());
            pal3.add(bd);
        }
        arrAd2.notifyDataSetChanged();
    }

    public void turnOnOff(View v) {
        if (!ba.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        } else {
//            ba.cancelDiscovery();
            b2.setEnabled(false);
            ba.disable();
//            pal.clear();
//            arrAd2.notifyDataSetChanged();
            b1.setText("Turn On BlueTooth");
            flag = false;
        }
    }

    public void makeDiscoverable(View view) {
        if (ba.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 720);
            startActivityForResult(intent, 2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            b2.setEnabled(true);
            b1.setText("Turn Off Bluetooth");
            getPairedDevices();
            flag = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (flag) {
                        ba.startDiscovery();
                    }
                }
            }).start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cust_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.server) {
            Intent intn = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intn);
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView adapterView, View view, int i, long l) {
        BluetoothDevice bd = pal3.get(i);
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra("Connect to", bd);
        startActivity(intent);
    }

    private boolean isMarshmallow() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isPermissionGiven() {
        if (isMarshmallow()) {//Check Point 10> to add the code for method isMarshmallow()
            if (!(checkSelfPermission(BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
//                    && checkSelfPermission(BLUETOOTH_PRIVILEGED)== PackageManager.PERMISSION_GRANTED
            )) {
                String[] perms = {"android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN"};
                int permsRequestCode = 200;
                requestPermissions(perms, permsRequestCode);
                if (checkSelfPermission(BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                        checkSelfPermission(BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
//                        && checkSelfPermission(BLUETOOTH_PRIVILEGED)== PackageManager.PERMISSION_GRANTED
                        )
                    return true;    //3>> When Permission granted after have been asked
                else
                    return false;    //4>> When Permission not granted after have been asked
            }
            return true;        //2>> When device has already granted permission
        }
        return true;            //1>> When device is not Marshmallow
    }
}
