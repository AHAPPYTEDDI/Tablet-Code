package com.example.spinatablet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 2;
    public static final int NEW_READING = 3;
    public static final int CONNECTIONFAILED = 4;
    public static final int CONNECTING = 5;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    final String TAG = "MainActivity";
    /* access modifiers changed from: private */
    static public Handler handler;
    static BluetoothClassicService bluetoothClassicService;
    static BluetoothLowEnergyService bluetoothLowEnergyService;
    static boolean boundToBluetoothClassicService;
    static boolean boundToBluetoothLowEnergyService;
    static TextView leftConnectivityText;
    static TextView leftValueText;
    static TextView rightConnectivityText;
    static TextView rightValueText;
    static TextView bleDevices;
    static HeatMap heatMap;
    private final MyServiceConnection blClassicServiceConnection = new MyServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            MainActivity.bluetoothClassicService = ((BluetoothClassicService.LocalBinder) service).getService();
            MainActivity.bluetoothClassicService.handler = MainActivity.handler;
            MainActivity.boundToBluetoothClassicService = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            MainActivity.boundToBluetoothClassicService = false;
        }
    };
    private final MyServiceConnection bleServiceConnection = new MyServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            MainActivity.bluetoothLowEnergyService = ((BluetoothLowEnergyService.LocalBinder) service).getService();
            MainActivity.bluetoothLowEnergyService.handler = MainActivity.handler;
            MainActivity.boundToBluetoothLowEnergyService = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            MainActivity.boundToBluetoothClassicService = false;
        }
    };

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView((int) C0633R.layout.activity_main);
        setContentView(C0633R.layout.activity_main);
//        TextView textView = (TextView) findViewById(R.id.leftState);
        leftConnectivityText = findViewById(R.id.leftStatus);
        leftConnectivityText.setText(MainActivity.this.getString(R.string.disconnected));

        rightConnectivityText = findViewById(R.id.rightStatus);
        rightConnectivityText.setText(MainActivity.this.getString(R.string.disconnected));
        leftValueText = findViewById(R.id.leftValue);
        rightValueText = findViewById(R.id.rightValue);
        bleDevices = findViewById(R.id.connectedBLEDevices);
        checkPermission();

        heatMap = findViewById(R.id.heatmap);
        heatMap.setMinimum(0.0);
        heatMap.setMaximum(100.0);
        Random rand = new Random();
        for (int i = 0; i < 20; i++) {
            HeatMap.DataPoint point = new HeatMap.DataPoint(rand.nextFloat(), rand.nextFloat(), rand.nextDouble() * 100.0);
            heatMap.addData(point);
        }

        handler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                Log.d(TAG, "Received message: " + msg.what + " " + msg.arg1 + " " + msg.arg2);
                if (msg.what == CONNECTED || msg.what == DISCONNECTED || msg.what == CONNECTING || msg.what == CONNECTIONFAILED) {
                    String connectionStateString = msg.what == CONNECTED ? "Connected" : msg.what == DISCONNECTED ? "Disconnected" : msg.what == CONNECTING ? "Connecting" : msg.what == CONNECTIONFAILED ? "Connecting Failed" : "No State";
                    int side = msg.arg1;
                    if (side == LEFT) {
                        MainActivity.leftConnectivityText.setText(connectionStateString);
                    } else if (side == RIGHT) {
                        MainActivity.rightConnectivityText.setText(connectionStateString);
                    }
                }
                if (msg.what == NEW_READING) {
                    int side3 = msg.arg1;
                    byte[] incHeaderAndFooter = (byte[]) msg.obj;
                    if (incHeaderAndFooter.length != 50) {
                        Log.d(TAG, "Should be 50");
                        return;
                    }
                    byte[] value = Arrays.copyOfRange(incHeaderAndFooter, 5, 45);
                    if (side3 == LEFT) {
//                        MainActivity.leftValueText.setText("values: " + value);
//                        MainActivity.leftValueText.setText("values: " + value);
                        UpdateRedGraph(side3, value);
                    } else if (side3 == RIGHT) {
                        MainActivity.rightConnectivityText.setText("values: " + value);
                        UpdateRedGraph(side3, value);
                    }
                }
            }
        };
    }

    void UpdateRedGraph(int side, byte[] values){
//        byte[] trimmedValues = Arrays.copyOfRange(values, 5, 45);
        MainActivity.leftValueText.setText("values: " + values[0] + " " + values[1]);
    }

    public void checkPermission() {
        Log.d(TAG, "debug checking permission");
        if (Build.VERSION.SDK_INT < 23) {
            Log.d(TAG, "debug SDK below 23");
            return;
        }
        if (checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED && checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED) {
            bindService(new Intent(this, BluetoothClassicService.class), blClassicServiceConnection, Context.BIND_AUTO_CREATE);
            bindService(new Intent(this, BluetoothLowEnergyService.class), bleServiceConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "starting bluetooth services");
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, 1);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != 1 || grantResults[0] != 0 || grantResults[1] != 0) {
            Log.d(TAG, "debug in request permi");
            checkPermission();
        }
    }
}
