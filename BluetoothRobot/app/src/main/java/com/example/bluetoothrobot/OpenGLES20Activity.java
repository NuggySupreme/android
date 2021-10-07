package com.example.bluetoothrobot;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Random;
import java.util.UUID;

public class OpenGLES20Activity extends AppCompatActivity {

    private ChartGLSurfaceView gLView;
    private final Handler customHandler = new Handler();

    private UUID robotUUID = null; //UUID for the bluetooth connection itself. This is needed to fully connect to the robot over bluetooth
    private BluetoothDevice robot;
    private BluetoothSocket robotConnection;
    private BufferedReader fromRobot; //gets input from robot
    private ReadThread read = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        robotUUID = UUID.fromString(getString(R.string.uuid));
        gLView = new ChartGLSurfaceView(this);
        setContentView(gLView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (robot != null) { //if we are coming in from OpenGLActivity with an already found robot
            (new ConnectThread()).start();
        }

        //updateTimerThread.run();

        //The following call resumes a paused rendering thread.
        //If you de-allocated graphic objects for onPause()
        //this is a good place to re-allocate them
        gLView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //The following call pauses the rendering thread.
        //If your OpenGL application is memory intensive,
        //you should consider de-allocating objects that
        //consume significant memory here
        closeConnection();
        gLView.onPause();
    }

    private void closeConnection() {
        try {
            if (fromRobot != null) {
                fromRobot.close();
            }
            if (robotConnection != null) {
                robotConnection.close();
            }
            if (read != null) {
                read.go = false;
            }
        } catch(Exception e) {
            Log.e("Error", "connection closing error");
            e.printStackTrace();
        }
    }

    private class ConnectThread extends Thread { //Sends a bluetooth connection to the robot
        public void run() {
            try {
                robotConnection = robot.createRfcommSocketToServiceRecord(robotUUID); //Send connection request to robot
                robotConnection.connect();
                Snackbar.make(findViewById(R.id.activity_main), "Connected to robot", Snackbar.LENGTH_LONG).show(); //Notify the user that the two devices are connecte
                fromRobot = new BufferedReader(new InputStreamReader(robotConnection.getInputStream()));
                (new ReadThread()).start();
            } catch (IOException e) {
                Log.e("Error", "connecting to robot error");
                e.printStackTrace();
            }
        }
    }

    private class ReadThread extends Thread { //Gets input from robot
        private boolean go = true;
        @Override

        public void run() {
            String junk = "";
            String trash = "";
            int count = 0;
            try {
                while (count < 20) {
                    junk = junk + fromRobot.readLine();
                    count++;
                }
            } catch(Exception e) {
                Log.e("error", "junk cleaning error");
            }
            while(go) {
                if(robotConnection != null) { //if the robot is connected
                    if (robotConnection.isConnected()) {
                        try {
                            final String message = fromRobot.readLine(); //get message from robot
                            if (message != null && message.startsWith("DATA:")) { //if the message is not empty
                                float[] data = gLView.getChartData();
                                for (int i = 1; i < data.length - 2; i += 2) {
                                    data[i] = data[i + 2];
                                }
                                String val = message.substring(5);
                                data[data.length - 1] = Float.parseFloat(val);
                                gLView.setChartData(data);
                                customHandler.postDelayed(this, 0);
                            }
                        } catch (IOException e) {
                            Log.e("error", "reading from robot error");
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    go = false;
                }
            }
        }
    }
}