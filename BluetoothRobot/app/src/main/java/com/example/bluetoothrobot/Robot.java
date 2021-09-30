package com.example.bluetoothrobot;

import android.bluetooth.BluetoothSocket;

public class Robot {
    private static BluetoothSocket robot = null;

    public static void setRobot(BluetoothSocket r) {
        robot = r;
    }

    public static BluetoothSocket getRobot() {
        return robot;
    }
}
