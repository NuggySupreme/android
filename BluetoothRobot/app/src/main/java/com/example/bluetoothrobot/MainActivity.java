package com.example.bluetoothrobot;

import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //IO Objects
    private ConnectThread connectThread = null; //thread to send connection request to robot
    private ReadThread readThread = null; //thread for reading input from robot
    private PrintWriter toRobot; //sends output to robot
    private BufferedReader fromRobot; //gets input from robot

    //UI Objects
    private EditText etMessage; //Input field to create message to send
    private TextView tvMessages; //TextView to show messages from robot. Acts as a message log

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
                    connectThread = new ConnectThread(); //start a connection to the robot
                    connectThread.start();
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

        //setup UI elements
        tvMessages = findViewById(R.id.tvMessages);
        tvMessages.setMovementMethod(new ScrollingMovementMethod()); //Set the message TextView to scroll to the newest text once there are enough lines
        etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);
        Button btnConnect = findViewById(R.id.btnConnect);

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
                    connectThread = new ConnectThread(); //Start a new connection to the robot
                    connectThread.start();
                } else { //Otherwise the robot wasn't already paired
                    Snackbar.make(findViewById(R.id.activity_main), "Starting Bluetooth discovery to find robot", Snackbar.LENGTH_LONG).show();
                    btAdapter.startDiscovery(); //find robot
                }

                tvMessages.setText(""); //clear message TextView
            }
        });

        btnSend.setOnClickListener(v -> { //Send message to robot
            String message = etMessage.getText().toString().trim(); //get message to send to robot and cleanup leading and trailing whitespace
            if(!message.isEmpty()) { //If there is a message to send
                new Thread(new WriteThread(message)).start(); //start thread to send message to robot
            }
        });
    }

    @Override
    protected void onDestroy() { //Cleanup on app close
        super.onDestroy();
        readThread.cancel();
        toRobot.close();
        connectThread.cancel();
        unregisterReceiver(receiver);
    }

    private class ConnectThread extends Thread { //Sends a bluetooth connection to the robot
        public void run() {
            btAdapter.cancelDiscovery();
            if((robotConnection != null && !robotConnection.isConnected()) || robotConnection == null) { //if there isn't already a connection to the robot or if there was a connection but it has since been closed
                try {
                    robotConnection = robot.createRfcommSocketToServiceRecord(robotUUID); //Send connection request to robot
                    robotConnection.connect();
                    Snackbar.make(findViewById(R.id.activity_main), "Connected to robot", Snackbar.LENGTH_LONG).show(); //Notify the user that the two devices are connected
                    toRobot = new PrintWriter(robotConnection.getOutputStream()); //Get output stream to send stuff to robot
                    fromRobot = new BufferedReader(new InputStreamReader(robotConnection.getInputStream())); //Get input stream to get stuff from robot

                    //Update UI element with text from input stream
                    runOnUiThread(() -> tvMessages.setText("Connected\n")); //Puts 'connected' message onto message log
                    readThread = new ReadThread(); //start a thread to handle input from robot
                    readThread.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            try {
                robotConnection.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReadThread extends Thread { //Gets input from robot
        @Override
        public void run() {
            while(true) {
                if(robotConnection.isConnected()) { //if the robot is connected
                    try {
                        final String message = fromRobot.readLine(); //get message from robot
                        if(message != null) { //if the message is not empty
                            runOnUiThread(() -> {
                                tvMessages.append("robot: " + message + "\n"); //append message to log
                            });
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                } else { //Otherwise robot connection needs to be restarted
                    connectThread = new ConnectThread();
                    connectThread.start();
                    return;
                }
            }
        }

        public void cancel() {
            try {
                fromRobot.close();
            } catch(IOException e) {
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
                tvMessages.append("android: " + message + "\n"); //append message to message log
                etMessage.setText("");
            });
        }
    }
}