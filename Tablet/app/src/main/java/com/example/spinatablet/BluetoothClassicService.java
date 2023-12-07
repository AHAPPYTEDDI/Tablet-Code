package com.example.spinatablet;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.Set;

public class BluetoothClassicService extends Service {
    public static final String leftDeviceName = "SPINA PediSol 250L_40_58E";
    public static final String rightDeviceName = "SPINA PediSol 250R_40_58F";
    final String TAG = "BluetoothClassicService";
    private final IBinder binder = new LocalBinder();
    BluetoothAdapter btAdapter;
    Runnable classicConnectionChecker = new Runnable() {
        public void run() {
            try {
                if (bothDevicesConnected()) {
                    Log.d("BluetoothClassicService", "Connected to both devices");
                    if (btAdapter.isDiscovering()) {
                        Log.d("BluetoothClassicService", "Cancelling discovery`");
                        btAdapter.cancelDiscovery();
                    }
                }
                else{
                    Log.d("BluetoothClassicService", "Haven't connected to both, restart scan");
                    Log.d("BluetoothClassicService", "Threads: " + leftConnectionThread + " " + rightConnectionThread);
                    Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
                    for(BluetoothDevice device : pairedDevices){
//                        Log.d(TAG, device.getName());
                        if (device.getName().equals(leftDeviceName)){
                            if (!IsThreadAlive(leftConnectionThread)){
                                leftConnectionThread = new BluetoothClassicConnectionThread(btAdapter, device,handler);
                                leftConnectionThread.start();
                            }
                        }
                        else if (device.getName().equals(rightDeviceName)){
                            if (!IsThreadAlive(rightConnectionThread)){
                                rightConnectionThread = new BluetoothClassicConnectionThread(btAdapter, device, handler);
                                rightConnectionThread.start();
                            }
                        }
                    }
                }
//            else if (!btAdapter.isDiscovering()) {
//                    Log.d("BluetoothClassicService", "Haven't connected to both, restart scan");
//                    Log.d("BluetoothClassicService", "" + leftConnectionThread + " " + rightConnectionThread);
//                    btAdapter.startDiscovery();
//                }
            } finally {
                classicConnectionCheckerHandler.postDelayed(classicConnectionChecker, 15000);
            }
        }
    };
    Handler classicConnectionCheckerHandler;
    Handler handler;
    BluetoothClassicConnectionThread leftConnectionThread;
    BluetoothClassicConnectionThread rightConnectionThread;
    SpinaBroadcastReceiver spinaBroadcastReceiver;

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        /* access modifiers changed from: package-private */
        public BluetoothClassicService getService() {
            return BluetoothClassicService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void onCreate() {
        super.onCreate();
        Log.d("BluetoothClassicService", "Created");
        handler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                Log.d(TAG, "bluetooth classic service handler message: " + msg);
            }
        };
        spinaBroadcastReceiver = new SpinaBroadcastReceiver();
        registerReceiver(spinaBroadcastReceiver, new IntentFilter("android.bluetooth.device.action.FOUND"));
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        classicConnectionCheckerHandler = new Handler();
        classicConnectionChecker.run();
    }

    public void onDestroy() {
        unregisterReceiver(spinaBroadcastReceiver);
    }

    /* access modifiers changed from: package-private */
    public boolean bothDevicesConnected() {
        return IsThreadAlive(leftConnectionThread);
//        return IsThreadAlive(leftConnectionThread) && IsThreadAlive(rightConnectionThread);
    }

    /* access modifiers changed from: package-private */
    public boolean IsThreadAlive(Thread thread) {
        if (thread == null) {
            return false;
        }
//        if (!thread.isAlive()){
//            thread = null;
//        }
//        return false;
        BluetoothClassicConnectionThread bcct = (BluetoothClassicConnectionThread) thread;
        return bcct.SocketConnected;
//        return thread.isAlive();
    }

    public class SpinaBroadcastReceiver extends BroadcastReceiver {
        public SpinaBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.bluetooth.device.action.FOUND".equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (device.getName() != null) {
                    if (device.getName().equals(leftDeviceName)) {
                        if (!IsThreadAlive(leftConnectionThread)) {
                            Log.d("BluetoothClassicService", "Started Left ConnectionThread");
                            leftConnectionThread = new BluetoothClassicConnectionThread(btAdapter, device,handler);
                            leftConnectionThread.start();
                        }
                    }
                    if (device.getName().equals(rightDeviceName)) {
                        if (!IsThreadAlive(rightConnectionThread)) {
                            Log.d("BluetoothClassicService", "Started Right ConnectionThread");
                            rightConnectionThread = new BluetoothClassicConnectionThread(btAdapter, device,handler);
                            rightConnectionThread.start();
                        }
                    }
                }
            }
        }
    }
}
