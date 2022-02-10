package com.example.bluetoothrobot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //IO Objects
    private PrintWriter toRobot; //sends output to robot
    private BufferedReader fromRobot; //gets input from robot
    private ReadThread read = null;

    //UI Objects
    private EditText etMessage; //Input field to create message to send
    //private SkeletonGLSurfaceView gLView;
    private TestView gLView;
    private final Handler customHandler = new Handler();

    //Bluetooth Objects
    private UUID robotUUID = null; //UUID for the bluetooth connection itself. This is needed to fully connect to the robot over bluetooth
    private BluetoothSocket robotConnection = null; //Bluetooth socket to communicate with the robot over
    private BluetoothDevice robot = null; //Bluetooth device representing the robot's bluetooth adapter
    private BluetoothAdapter btAdapter = null; //local device's bluetooth adapter

    private final BroadcastReceiver receiver = new BroadcastReceiver() { //receiver to receive content from device discovery
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) { //if action from filter is a Bluetooth Device Found action, check if the device is the robot. If so, make a connection
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceHardwareAddress = device.getAddress();
                if (deviceHardwareAddress.equalsIgnoreCase(getString(R.string.robotHardwareAddress))) { //This is the MAC Address for the bluetooth adapter on the robot
                    robot = device; //This means that the robot was found during Bluetooth discovery and the app should connect to it
                    (new ConnectThread()).start();
                }
            }
        }
    };

    private boolean updating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //create app
        super.onCreate(savedInstanceState);
        //gLView = new SkeletonGLSurfaceView(this);
        gLView = new TestView(this);

        setContentView(R.layout.activity_main);

        //FrameLayout frm1 = (FrameLayout) findViewById(R.id.skeletonFrame);
        //frm1.addView(gLView);

       // FrameLayout frm2 = (FrameLayout) findViewById(R.id.chartFrame);
        //frm2.addView(gLView);

        FrameLayout frm3 = (FrameLayout) findViewById(R.id.controlFrame);
        //frm3.addView(gLView);

        FrameLayout frm4 = (FrameLayout) findViewById(R.id.otherFrame);
        //frm4.addView(gLView);

        //setup Bluetooth and register discovery filter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        robotUUID = UUID.fromString(getString(R.string.uuid));
        Intent intent = getIntent();
        if (intent.hasExtra("btDevice")) {
            robot = intent.getParcelableExtra("btDevice");
        }

        //setup UI elements
        etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);
        Button btnConnect = findViewById(R.id.btnConnect);
        Button btnClose = findViewById(R.id.btnClose);

        //Set click event listeners for buttons
        btnConnect.setOnClickListener(v -> {
            if (!btAdapter.isEnabled()) { //If bluetooth on the tablet is turned off, show a message to turn it on instead of crashing
                Snackbar.make(findViewById(R.id.activity_main), "Please enable Bluetooth in the settings", Snackbar.LENGTH_LONG).show();
            } else {
                Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices(); //Check if the robot is already paired with the tablet.

                if (pairedDevices.size() > 0 && robot == null) {
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceHardwareAddress = device.getAddress();
                        if (deviceHardwareAddress.equalsIgnoreCase(getString(R.string.robotHardwareAddress))) {
                            robot = device;
                        }
                    }
                }

                if (robot != null) { //If the robot was already paired:
                    //attempt connection
                    (new ConnectThread()).start();
                } else { //Otherwise the robot wasn't already paired
                    Snackbar.make(findViewById(R.id.activity_main), "Starting Bluetooth discovery to find robot", Snackbar.LENGTH_LONG).show();
                    btAdapter.startDiscovery(); //find robot
                }
            }
        });

        btnSend.setOnClickListener(v -> { //Send message to robot
            String message = etMessage.getText().toString().trim(); //get message to send to robot and cleanup leading and trailing whitespace
            if (toRobot != null) {
                if (!message.isEmpty()) { //If there is a message to send
                    new Thread(new WriteThread(message)).start(); //start thread to send message to robot
                }
            } else {
                Snackbar.make(findViewById(R.id.activity_main), "Please connect to the robot", Snackbar.LENGTH_LONG).show();
            }
        });

        btnClose.setOnClickListener(v -> closeConnection());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (robot != null) { //if we are coming in from OpenGLActivity with an already found robot
            Log.i("Info", "got bluetooth connection");
            (new ConnectThread()).start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //gLView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //gLView.onPause();
    }

    @Override
    protected void onDestroy() { //Cleanup on app close
        super.onDestroy();
        closeConnection();
        unregisterReceiver(receiver);
    }

    private void closeConnection() {
        try {
            if (robot != null) {
                robot = null;
            }
            if (robotConnection != null) {
                robotConnection.close();
            }
            if (toRobot != null) {
                toRobot.close();
            }
            if (fromRobot != null) {
                fromRobot.close();
            }
            if (read != null) {
                read.interrupt();
            }
        } catch (Exception e) {
            Log.e("Error", "connection closing error");
            e.printStackTrace();
        }
    }

    private float[] createLeftArm(String[] p) {
        float[] toReturn = new float[12];

        int j = 0;
        for(int i = 2; i < 9; i++) {
            toReturn[j] = Float.parseFloat(p[3*i]);
            toReturn[j + 1] = Float.parseFloat(p[3*i + 1]);
            if(i == 2) {
                i += 3;
            }
            j += 3;
        }
        return toReturn;
    }

    private float[] createLeftLeg(String[] p) {
        float[] toReturn = new float[12];
        int j = 0;
        for(int i = 5; i < 15; i++) {
            toReturn[j] = Float.parseFloat(p[3 * i]);
            toReturn[j + 1] = Float.parseFloat(p[3 * i + 1]);
            //toReturn[j + 2] = Float.parseFloat(p[3 * i + 2]);
            j += 3;
            if(i == 5) {
                i += 6; //skip to joint 6
            }
        }
        return toReturn;
    }

    private float[] createRightArm(String[] p) {
        float[] toReturn = new float[12];

        int j = 0;
        for(int i = 2; i < 12; i++) {
            toReturn[j] = Float.parseFloat(p[3*i]);
            toReturn[j + 1] = Float.parseFloat(p[3*i + 1]);
            if(i == 2) {
                i += 6;
            }
            j += 3;
        }
        return toReturn;
    }

    private float[] createRightLeg(String[] p) {
        float[] toReturn = new float[12];
        int j = 0;
        for(int i = 5; i < 18; i++) {
            toReturn[j] = Float.parseFloat(p[3 * i]);
            toReturn[j + 1] = Float.parseFloat(p[3 * i + 1]);
            //toReturn[j + 2] = Float.parseFloat(p[3 * i + 2]);
            j += 3;
            if(i == 5) {
                i += 9; //skip to joint 6
            }
        }
        return toReturn;
    }

    private float[] createSpine(String[] p) {
        float[] toReturn = new float[18];

        for(int i = 0; i < 6; i++) {
            toReturn[3*i] = Float.parseFloat(p[3*i]);
            toReturn[3*i + 1] = Float.parseFloat(p[3*i + 1]);
        }
        return toReturn;
    }

    public static int max(int a, int b, int c, int d, int e) {
        int max = a;
        max = Math.max(max, b);
        max = Math.max(max, c);
        max = Math.max(max, d);
        max = Math.max(max, e);
        return max;
    }

    private class ConnectThread extends Thread { //Sends a bluetooth connection to the robot
        public void run() {
            btAdapter.cancelDiscovery();
            try {
                robotConnection = robot.createRfcommSocketToServiceRecord(robotUUID); //Send connection request to robot
                robotConnection.connect();
                Snackbar.make(findViewById(R.id.activity_main), "Connected to robot", Snackbar.LENGTH_LONG).show(); //Notify the user that the two devices are connecte
                toRobot = new PrintWriter(robotConnection.getOutputStream()); //Get output stream to send stuff to robot
                fromRobot = new BufferedReader(new InputStreamReader(robotConnection.getInputStream()));
                read = new ReadThread();
                read.start();
            } catch (IOException e) {
                Log.e("Error", "connecting to robot error");
                e.printStackTrace();
            }
        }
    }

    private class WriteThread implements Runnable {
        private final String message;

        public WriteThread(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            toRobot.write(message);
            toRobot.flush(); //send message over bluetooth to robot
            runOnUiThread(() -> etMessage.setText(""));
        }
    }
    private class ReadThread extends Thread { //Gets input from robot

        @Override
        public void run() {
            while(!isInterrupted()) {
                try {
                    String message = fromRobot.readLine(); //get message from robot
                    if (message.startsWith("DATA:") && !updating) { //if the message is not empt
                        message = message.substring(5);
                        String[] points = message.split(",");

                        float[] lA = createLeftArm(points);
                        float[] lL = createLeftLeg(points);
                        float[] rA = createRightArm(points);
                        float[] rL = createRightLeg(points);
                        float[] spine = createSpine(points);

                        float[] maxVal = {Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE};
                        float[] minVal = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};

                        for(int i = 0; i < max(lA.length, lL.length, rA.length, rL.length, spine.length); i += 3) {
                            if(i < lA.length) {
                                minVal[0] = Math.min(minVal[0], lA[i]);
                                maxVal[0] = Math.max(maxVal[0], lA[i]);
                                minVal[1] = Math.min(minVal[1], lA[i + 1]);
                                maxVal[1] = Math.max(maxVal[1], lA[i + 1]);
                            }
                            if(i < lL.length) {
                                minVal[0] = Math.min(minVal[0], lL[i]);
                                maxVal[0] = Math.max(maxVal[0], lL[i]);
                                minVal[1] = Math.min(minVal[1], lL[i + 1]);
                                maxVal[1] = Math.max(maxVal[1], lL[i + 1]);
                            }
                            if(i < rA.length) {
                                minVal[0] = Math.min(minVal[0], rA[i]);
                                maxVal[0] = Math.max(maxVal[0], rA[i]);
                                minVal[1] = Math.min(minVal[1], rA[i + 1]);
                                maxVal[1] = Math.max(maxVal[1], rA[i + 1]);
                            }
                            if(i < rL.length) {
                                minVal[0] = Math.min(minVal[0], rL[i]);
                                maxVal[0] = Math.max(maxVal[0], rL[i]);
                                minVal[1] = Math.min(minVal[1], rL[i + 1]);
                                maxVal[1] = Math.max(maxVal[1], rL[i + 1]);
                            }
                            if(i < spine.length) {
                                minVal[0] = Math.min(minVal[0], spine[i]);
                                maxVal[0] = Math.max(maxVal[0], spine[i]);
                                minVal[1] = Math.min(minVal[1], spine[i + 1]);
                                maxVal[1] = Math.max(maxVal[1], spine[i + 1]);
                            }
                        }
                        //gLView.setSkeletonData(minVal, maxVal, lA, lL, rA, rL, spine);
                        customHandler.postDelayed(this, 0);
                        updating = false;
                    }
                } catch (IOException e) {
                    Log.e("error", "reading from robot error");
                    read.interrupt();
                    e.printStackTrace();
                }
            }
        }
    }
}