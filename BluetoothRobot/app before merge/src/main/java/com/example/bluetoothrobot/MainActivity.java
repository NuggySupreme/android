package com.example.bluetoothrobot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //IO Objects
    private PrintWriter toRobot; //sends output to robot

    //UI Objects
    private EditText etMessage; //Input field to create message to send

    //Bluetooth Objects
    private UUID robotUUID = null; //UUID for the bluetooth connection itself. This is needed to fully connect to the robot over bluetooth
    private BluetoothSocket robotConnection = null; //Bluetooth socket to communicate with the robot over
    private BluetoothDevice robot = null; //Bluetooth device representing the robot's bluetooth adapter
    private BluetoothAdapter btAdapter = null; //local device's bluetooth adapter

    private final BroadcastReceiver receiver = new BroadcastReceiver(){ //receiver to receive content from device discovery
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) { //if action from filter is a Bluetooth Device Found action, check if the device is the robot. If so, make a connection
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceHardwareAddress = device.getAddress();
                if(deviceHardwareAddress.equalsIgnoreCase(getString(R.string.robotHardwareAddress))) { //This is the MAC Address for the bluetooth adapter on the robot
                    robot = device; //This means that the robot was found during Bluetooth discovery and the app should connect to it
                    (new ConnectThread()).start();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //create app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup Bluetooth and register discovery filter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        robotUUID = UUID.fromString(getString(R.string.uuid));
        Intent intent = getIntent();
        if(intent.hasExtra("btDevice")) {
            robot = intent.getParcelableExtra("btDevice");
        }

        //setup UI elements
        etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);
        Button btnConnect = findViewById(R.id.btnConnect);
        Button btnClose = findViewById(R.id.btnClose);
        Button btnSwitch = findViewById(R.id.btnSwitch);

        //Set click event listeners for buttons
        btnConnect.setOnClickListener(v -> {

            if(!btAdapter.isEnabled()) { //If bluetooth on the tablet is turned off, show a message to turn it on instead of crashing
                Snackbar.make(findViewById(R.id.activity_main), "Please enable Bluetooth in the settings", Snackbar.LENGTH_LONG).show();
            }

            else {
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
            if(toRobot != null) {
                if (!message.isEmpty()) { //If there is a message to send
                    new Thread(new WriteThread(message)).start(); //start thread to send message to robot
                }
            } else {
                Snackbar.make(findViewById(R.id.activity_main), "Please connect to the robot", Snackbar.LENGTH_LONG).show();
            }
        });

        btnClose.setOnClickListener(v -> closeConnection());

        btnSwitch.setOnClickListener(v -> {
            Intent GLIntent = new Intent(getApplicationContext(), SkeletonGLES20Activity.class);
            GLIntent.putExtra("btDevice", robot);
            closeConnection();
            startActivity(GLIntent);
        });
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
    protected void onDestroy() { //Cleanup on app close
        super.onDestroy();
        closeConnection();
        unregisterReceiver(receiver);
    }

    private void closeConnection() {
        try {
            if(robot != null) {
                robot = null;
            }
            if(robotConnection != null) {
                robotConnection.close();
            }
            if(toRobot != null) {
                toRobot.close();
            }
        } catch(Exception e) {
            Log.e("Error", "connection closing error");
            e.printStackTrace();
        }
    }

    private class ConnectThread extends Thread { //Sends a bluetooth connection to the robot
        public void run() {
            btAdapter.cancelDiscovery();
            try {
                robotConnection = robot.createRfcommSocketToServiceRecord(robotUUID); //Send connection request to robot
                robotConnection.connect();
                Snackbar.make(findViewById(R.id.activity_main), "Connected to robot", Snackbar.LENGTH_LONG).show(); //Notify the user that the two devices are connecte
                toRobot = new PrintWriter(robotConnection.getOutputStream()); //Get output stream to send stuff to robot
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
            runOnUiThread(() -> {
                etMessage.setText("");
            });
        }
    }
}