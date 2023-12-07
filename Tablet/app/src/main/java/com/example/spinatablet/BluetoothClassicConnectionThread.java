package com.example.spinatablet;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.UUID;

public class BluetoothClassicConnectionThread extends Thread {
    private static final UUID SPINA_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final String TAG = "BLClassicConnectThread";
    BluetoothAdapter btAdapter;
    BluetoothDevice device;
    Handler handler;
    InputStream inputStream;
    long lastRead;
    final long restartTime = 10000;
    BluetoothSocket socket;

    public boolean SocketConnected = false;

    public BluetoothClassicConnectionThread(BluetoothAdapter btAdapter2, BluetoothDevice device2, Handler handler2) {
        btAdapter = btAdapter2;
        device = device2;
        handler = handler2;
        Log.d("BLClassicConnectThread", "NEW BLCONNECTION THREAD CREATED FOR " + device.getName());
    }

    public void run() {
        try {
            Log.d("BLClassicConnectThread", "Connecting to " + device.getName());
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SPINA_UUID);
            if (device.getName().equals(BluetoothClassicService.leftDeviceName)) {
                handler.obtainMessage(MainActivity.CONNECTING, MainActivity.LEFT, 0).sendToTarget();
            } else if (device.getName().equals(BluetoothClassicService.rightDeviceName)) {
                handler.obtainMessage(MainActivity.CONNECTING, MainActivity.RIGHT, 0).sendToTarget();
            }
            try {
                Log.d("BLClassicConnectThread", "Trying to connect to " + device.getName());
                socket.connect();
            } catch (IOException e) {
                Log.d("BLClassicConnectThread", "Failed to connect. Destroying thread " + e);
                if (device.getName().equals(BluetoothClassicService.leftDeviceName)) {
                    handler.obtainMessage(MainActivity.CONNECTIONFAILED, MainActivity.LEFT, 0).sendToTarget();
                } else if (device.getName().equals(BluetoothClassicService.rightDeviceName)) {
                    handler.obtainMessage(MainActivity.CONNECTIONFAILED, MainActivity.RIGHT, 0).sendToTarget();
                }
                return;
            }
            Log.d("BLClassicConnectThread", "Socket Connected: " + device.getName());
            SocketConnected = true;
            if (device.getName().equals(BluetoothClassicService.leftDeviceName)) {
                handler.obtainMessage(MainActivity.CONNECTED, MainActivity.LEFT, 0).sendToTarget();
            }
            if (device.getName().equals(BluetoothClassicService.rightDeviceName)) {
                handler.obtainMessage(MainActivity.CONNECTED, MainActivity.RIGHT, 0).sendToTarget();
            }
            inputStream = socket.getInputStream();
            Log.d("BLClassicConnectThread", "Input Stream Established: " + device.getName());
            lastRead = Calendar.getInstance().getTimeInMillis();
            while (!interrupted()) {
                while (inputStream.available() == 0) {
//                    Log.d("BLClassicConnectThread","InputStream is empty " + device.getName());
                    try {
                        long diff = Calendar.getInstance().getTimeInMillis() - lastRead;
                        int waitMillis = 10000;
                        if (diff > waitMillis) {
                            Log.d("BLClassicConnectThread", "Inactive for " + diff + " / " + diff + " on " + device.getName());
                            Thread.sleep(250);
                            if (device.getName().equals(BluetoothClassicService.leftDeviceName)) {
                                handler.obtainMessage(MainActivity.DISCONNECTED, MainActivity.LEFT, 0).sendToTarget();
                            }
                            if (device.getName().equals(BluetoothClassicService.rightDeviceName)) {
                                handler.obtainMessage(MainActivity.DISCONNECTED, MainActivity.RIGHT, 0).sendToTarget();
                            }
                            return;
                        }
                    } catch (Exception e) {
                        Log.e("BLClassicConnectThread", device.getName() + " : Connection Lost", e);
                        if (device.getName().equals(BluetoothClassicService.leftDeviceName)) {
                            handler.obtainMessage(MainActivity.DISCONNECTED, MainActivity.LEFT, 0).sendToTarget();
                        }
                        if (device.getName().equals(BluetoothClassicService.rightDeviceName)) {
                            handler.obtainMessage(MainActivity.DISCONNECTED, MainActivity.RIGHT, 0).sendToTarget();
                        }
                        if (device.getName().equals(BluetoothClassicService.leftDeviceName)) {
                            handler.obtainMessage(MainActivity.DISCONNECTED, MainActivity.LEFT, 0).sendToTarget();
                        }
                        if (device.getName().equals(BluetoothClassicService.rightDeviceName)) {
                            handler.obtainMessage(MainActivity.DISCONNECTED, MainActivity.RIGHT, 0).sendToTarget();
                        }
                        return;
                    }
                }
                int bytesAvailable = inputStream.available();
                if (bytesAvailable != 0) {
                    byte[] buffer = new byte[bytesAvailable];
                    inputStream.read(buffer, 0, bytesAvailable);
                    lastRead = Calendar.getInstance().getTimeInMillis();
//                    int byteTodec = Util.byteTodec(buffer)[0];
                    Log.d("BLClassicConnectThread", device.getName() + " buffer size: " + buffer.length + " : left data");
                    if (device.getName().equals(BluetoothClassicService.leftDeviceName)) {
                        handler.obtainMessage(MainActivity.NEW_READING, MainActivity.LEFT, 0, buffer).sendToTarget();
                        BluetoothLowEnergyService.SetLeftData(buffer);
                        handler.obtainMessage(MainActivity.CONNECTED, MainActivity.LEFT, 0).sendToTarget();
                    } else if (device.getName().equals(BluetoothClassicService.rightDeviceName)) {
                        handler.obtainMessage(MainActivity.NEW_READING, MainActivity.RIGHT, 0, buffer).sendToTarget();
                        BluetoothLowEnergyService.SetRightData(buffer);
                        handler.obtainMessage(MainActivity.CONNECTED, MainActivity.RIGHT, 0).sendToTarget();
                    }
                }
            }
            if (device.getName().equals(BluetoothClassicService.leftDeviceName)) {
                handler.obtainMessage(MainActivity.DISCONNECTED, MainActivity.LEFT, 0).sendToTarget();
            }
            if (device.getName().equals(BluetoothClassicService.rightDeviceName)) {
                handler.obtainMessage(MainActivity.DISCONNECTED, MainActivity.RIGHT, 0).sendToTarget();
            }
        } catch (IOException e2) {
            Log.e("BLClassicConnectThread", "Exception: ", e2);
            if (device.getName().equals(BluetoothClassicService.leftDeviceName)) {
                handler.obtainMessage(MainActivity.DISCONNECTED, MainActivity.LEFT, 0).sendToTarget();
            }
            if (device.getName().equals(BluetoothClassicService.rightDeviceName)) {
                handler.obtainMessage(MainActivity.DISCONNECTED, MainActivity.RIGHT, 0).sendToTarget();
            }
        } catch (Throwable th) {
            Log.e("BLClassicConnectThread", "Exception: ", th);
            if (device.getName().equals(BluetoothClassicService.leftDeviceName)) {
                handler.obtainMessage(MainActivity.DISCONNECTED, MainActivity.LEFT, 0).sendToTarget();
            }
            if (device.getName().equals(BluetoothClassicService.rightDeviceName)) {
                handler.obtainMessage(MainActivity.DISCONNECTED, MainActivity.RIGHT, 0).sendToTarget();
            }
        }
    }
}
