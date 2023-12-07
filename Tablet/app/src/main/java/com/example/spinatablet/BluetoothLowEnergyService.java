package com.example.spinatablet;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import androidx.core.view.InputDeviceCompat;

public class BluetoothLowEnergyService extends Service {
    static final String TAG = "BLEService";
    static BluetoothGattServer bluetoothGattServer;
    static List<BluetoothDevice> connectedDevices = new ArrayList();
    static BluetoothGattCharacteristic heartbeatCharacterstic;
    static BluetoothGattCharacteristic leftCharacteristic;
    static byte[] leftData;
    static BluetoothGattCharacteristic leftSecondCharacteristic;
    static BluetoothGattCharacteristic rightCharacteristic;
    static byte[] rightData;
    static BluetoothGattCharacteristic rightSecondCharacteristic;
    UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    AdvertiseCallback adCallback = new AdvertiseCallback() {
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(BluetoothLowEnergyService.TAG, "BLE Advertisement added successfully");
        }

        public void onStartFailure(int errorCode) {
            Log.e(BluetoothLowEnergyService.TAG, "Failed to add BLE advertisement, reason: " + errorCode);
        }
    };
    private final IBinder binder = new LocalBinder();
    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;
    BluetoothGattServerCallback callback = new BluetoothGattServerCallback() {
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            byte[] returnValue;
            if (BluetoothLowEnergyService.this.CLIENT_CONFIG.equals(descriptor.getUuid())) {
                Log.d(BluetoothLowEnergyService.TAG, "Config descriptor read");
                if (BluetoothLowEnergyService.connectedDevices.contains(device)) {
                    returnValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                } else {
                    returnValue = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                }
                BluetoothLowEnergyService.bluetoothGattServer.sendResponse(device, requestId, InputDeviceCompat.SOURCE_KEYBOARD, 0, returnValue);
                return;
            }
            Log.w(BluetoothLowEnergyService.TAG, "Unknown descriptor read request");
            BluetoothLowEnergyService.bluetoothGattServer.sendResponse(device, requestId, InputDeviceCompat.SOURCE_KEYBOARD, 0, null);
        }

        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            if (BluetoothLowEnergyService.this.CLIENT_CONFIG.equals(descriptor.getUuid())) {
                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                    Log.d(BluetoothLowEnergyService.TAG, "Subscribe device to notifications: " + device);
                    if (!BluetoothLowEnergyService.connectedDevices.contains(device)) {
                        BluetoothLowEnergyService.connectedDevices.add(device);
                    }
                } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
                    Log.d(BluetoothLowEnergyService.TAG, "Unsubscribe device from notifications: " + device);
                    BluetoothLowEnergyService.connectedDevices.remove(device);
                }
                BluetoothLowEnergyService.bluetoothGattServer.sendResponse(device, requestId, 0, 0, null);
                return;
            }
            Log.w(BluetoothLowEnergyService.TAG, "Unknown descriptor write request");
            if (responseNeeded) {
                BluetoothLowEnergyService.bluetoothGattServer.sendResponse(device, requestId, InputDeviceCompat.SOURCE_KEYBOARD, 0, null);
            }
        }

        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (newState == 2) {
                if (!BluetoothLowEnergyService.connectedDevices.contains(device)) {
                    BluetoothLowEnergyService.connectedDevices.add(device);
                }
                Log.d(BluetoothLowEnergyService.TAG, "connected");
            }
            if (newState == 0) {
                BluetoothLowEnergyService.connectedDevices.remove(device);
            }
        }

        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.i(BluetoothLowEnergyService.TAG, "onCharacteristicReadRequest " + characteristic.getUuid().toString());
        }
    };
    Handler handler;
    UUID heartbeatDescriptorUUID = UUID.fromString("44f33a30-30d6-4b31-ac89-9555b39beb13");
    UUID heartbeatUUID = UUID.fromString("34f33a30-30d6-4b31-ac89-9555b39beb13");
    UUID leftDescriptorUUID = UUID.fromString("79654fa2-2b25-4bd8-b260-85b0e67141c2");
    UUID rightDescriptorUUID = UUID.fromString("69654fa2-2b25-4bd8-b260-85b0e67141c2");
    BluetoothGattService service;
    UUID spinaLeftSecondUUID = UUID.fromString("9ede68ba-bc6a-417e-b7f3-e5c591a9748f");
    UUID spinaLeftUUID = UUID.fromString("9ede68ba-bc6a-417e-b7f3-e5c591a9748e");
    UUID spinaRightSecondUUID = UUID.fromString("6262946c-da31-44f2-a1b5-51ce4f96b49f");
    UUID spinaRightUUID = UUID.fromString("6262946c-da31-44f2-a1b5-51ce4f96b499");
    UUID spinaServiceUUID = UUID.fromString("84f33a30-30d6-4b31-ac89-9555b39beb13");

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        /* access modifiers changed from: package-private */
        public BluetoothLowEnergyService getService() {
            return BluetoothLowEnergyService.this;
        }
    }

    public static void SetLeftData(byte[] newLeftData) {
        byte[] copyOfRange = Arrays.copyOfRange(newLeftData, 5, 45);
        leftData = copyOfRange;
        byte[] leftSecondData = Arrays.copyOfRange(copyOfRange, 20, 40);
        leftCharacteristic.setValue(leftData);
        leftSecondCharacteristic.setValue(leftSecondData);
        for (BluetoothDevice blDevice : connectedDevices) {
            bluetoothGattServer.notifyCharacteristicChanged(blDevice, leftCharacteristic, false);
            bluetoothGattServer.notifyCharacteristicChanged(blDevice, leftSecondCharacteristic, false);
        }
    }

    public static void SetRightData(byte[] newRightData) {
        byte[] copyOfRange = Arrays.copyOfRange(newRightData, 5, 45);
        rightData = copyOfRange;
        byte[] rightSecondData = Arrays.copyOfRange(copyOfRange, 20, 40);
        rightCharacteristic.setValue(rightData);
        rightSecondCharacteristic.setValue(rightSecondData);
        for (BluetoothDevice blDevice : connectedDevices) {
            bluetoothGattServer.notifyCharacteristicChanged(blDevice, rightCharacteristic, false);
            bluetoothGattServer.notifyCharacteristicChanged(blDevice, rightSecondCharacteristic, false);
        }
    }

    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Created");
        BluetoothManager bluetoothManager2 = getSystemService(BluetoothManager.class);
        bluetoothManager = bluetoothManager2;
        bluetoothAdapter = bluetoothManager2.getAdapter();
        SetupCharacteristics();
        StartAdvertising();
        final Handler handler2 = new Handler();
        new Timer().schedule(new TimerTask() {
            public void run() {
                handler2.post(new Runnable() {
                    public void run() {
                        try {
                            for (BluetoothDevice blDevice : BluetoothLowEnergyService.connectedDevices) {
                                Log.d(BluetoothLowEnergyService.TAG, "updating heartbeat");
                                BluetoothLowEnergyService.heartbeatCharacterstic.setValue(" ");
                                BluetoothLowEnergyService.bluetoothGattServer.notifyCharacteristicChanged(blDevice, BluetoothLowEnergyService.heartbeatCharacterstic, false);
                            }
                        } catch (Exception e) {
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    /* access modifiers changed from: package-private */
    public void StartAdvertising() {
        bluetoothAdapter.getBluetoothLeAdvertiser().startAdvertising(new AdvertiseSettings.Builder().setConnectable(true).build(), new AdvertiseData.Builder().setIncludeDeviceName(true).setIncludeTxPowerLevel(true).build(), new AdvertiseData.Builder().addServiceUuid(new ParcelUuid(spinaServiceUUID)).setIncludeTxPowerLevel(true).build(), adCallback);
    }

    /* access modifiers changed from: package-private */
    public void SetupCharacteristics() {
        bluetoothGattServer = bluetoothManager.openGattServer(getApplicationContext(), callback);
        service = new BluetoothGattService(spinaServiceUUID, 0);
        leftCharacteristic = new BluetoothGattCharacteristic(spinaLeftUUID, 18, 1);
        rightCharacteristic = new BluetoothGattCharacteristic(spinaRightUUID, 18, 1);
        leftSecondCharacteristic = new BluetoothGattCharacteristic(spinaLeftSecondUUID, 18, 1);
        rightSecondCharacteristic = new BluetoothGattCharacteristic(spinaRightSecondUUID, 18, 1);
        heartbeatCharacterstic = new BluetoothGattCharacteristic(heartbeatUUID, 18, 1);
        BluetoothGattDescriptor leftDescriptor = new BluetoothGattDescriptor(leftDescriptorUUID, 17);
        BluetoothGattDescriptor rightDescriptor = new BluetoothGattDescriptor(rightDescriptorUUID, 17);
        BluetoothGattDescriptor heartbeatDescriptor = new BluetoothGattDescriptor(heartbeatDescriptorUUID, 17);
        leftCharacteristic.addDescriptor(leftDescriptor);
        rightCharacteristic.addDescriptor(rightDescriptor);
        heartbeatCharacterstic.addDescriptor(heartbeatDescriptor);
        leftSecondCharacteristic.addDescriptor(leftDescriptor);
        rightSecondCharacteristic.addDescriptor(rightDescriptor);
        service.addCharacteristic(leftCharacteristic);
        service.addCharacteristic(rightCharacteristic);
        service.addCharacteristic(leftSecondCharacteristic);
        service.addCharacteristic(rightSecondCharacteristic);
        service.addCharacteristic(heartbeatCharacterstic);
        bluetoothGattServer.addService(service);
    }
}
