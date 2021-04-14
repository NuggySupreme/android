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
    private WriteThread writeThread = null;
    private PrintWriter toRobot; //sends output to robot
    private BufferedReader fromRobot; //gets input from robot

    //UI Objects
    private EditText etMessage; //Input field to create message to send
    private TextView tvMessages; //TextView to show messages from robot

    //Bluetooth Objects
    private UUID robotUUID = null; //UUID for the bluetooth connection itself
    private BluetoothSocket robotConnection = null; //Bluetooth socket to communicate with the robot over
    private BluetoothDevice robot = null; //Bluetooth device representing the robot's bluetooth adapter
    private BluetoothAdapter btAdapter = null; //local device's bluetooth adapter

    private final BroadcastReceiver receiver = new BroadcastReceiver(){ //receiver to receive content from device discovery
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) { //if action from filter is a Bluetooth Device Found action, check if the device is the robot. If so, make a connection
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceHardwareAddress = device.getAddress();
                if(deviceHardwareAddress.equalsIgnoreCase(getString(R.string.robotHardwareAddress))) {
                    robot = device;
                    connectThread = new ConnectThread();
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
        tvMessages.setMovementMethod(new ScrollingMovementMethod());
        etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);
        Button btnConnect = findViewById(R.id.btnConnect);
        Button btnClose = findViewById(R.id.btnClose);

        //Set click event listeners for buttons
        btnConnect.setOnClickListener(v -> {
            /*if(!btAdapter.isEnabled()) {
                //Make snackbar to enable bluetooth
            }\

            if bluetooth is disabled, make toast telling to enable bluetooth. else make connection
             */
            if(!btAdapter.isEnabled()) {
                Snackbar.make(findViewById(R.id.activity_main), "Please enable Bluetooth in the settings", Snackbar.LENGTH_LONG).show();
            }

            else {
                Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

                if (pairedDevices.size() > 0 && robot == null) {
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceHardwareAddress = device.getAddress();
                        if (deviceHardwareAddress.equalsIgnoreCase(getString(R.string.robotHardwareAddress))) {
                            robot = device;
                        }
                    }
                }

                if (robot != null) {
                    //attempt connection
                    connectThread = new ConnectThread(); //Start a new connection to the robot
                    connectThread.start();
                } else {
                    Snackbar.make(findViewById(R.id.activity_main), "Starting Bluetooth discovery to find robot", Snackbar.LENGTH_LONG).show();
                    btAdapter.startDiscovery(); //find robot
                }

                tvMessages.setText(""); //clear message TextView
            }
        });

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim(); //get message to send to robot and cleanup leading and trailing whitespace
            System.out.println(message);
            if(!message.isEmpty()) {
                writeThread = new WriteThread(message); //start thread to send message to robot
                writeThread.start();
            }
        });

        btnClose.setOnClickListener(v -> {
            closeConnection();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeConnection();
        unregisterReceiver(receiver);
    }

    private void closeConnection() {
        toRobot.close();
    }

    private class ConnectThread extends Thread { //Sends a bluetooth connection to the robot
        public void run() {
            btAdapter.cancelDiscovery();
            if((robotConnection != null && !robotConnection.isConnected()) || robotConnection == null) { //if there isn't already a connection to the robot or if there was a connection but it has since been closed
                try {
                    robotConnection = robot.createRfcommSocketToServiceRecord(robotUUID); //Send connection request to robot
                    robotConnection.connect();
                    Snackbar.make(findViewById(R.id.activity_main), "Connected to robot", Snackbar.LENGTH_LONG).show();
                    toRobot = new PrintWriter(robotConnection.getOutputStream(), true); //Get output stream to send stuff to robot and set it to auto flush data
                    fromRobot = new BufferedReader(new InputStreamReader(robotConnection.getInputStream())); //Get input stream to get stuff from robot

                    //Update UI element with text from input stream
                    runOnUiThread(() -> tvMessages.setText("Connected\n"));
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

    private class WriteThread extends Thread {
        private final String message;

        public WriteThread(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            toRobot.write(message);
            toRobot.flush();
            runOnUiThread(() -> {
                tvMessages.append("android: " + message + "\n");
                etMessage.setText("");
            });
        }
    }
}