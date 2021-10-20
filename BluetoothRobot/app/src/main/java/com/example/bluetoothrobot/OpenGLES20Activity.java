package com.example.bluetoothrobot;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

        Intent intent = getIntent();
        if(intent.hasExtra("btDevice")) {
            robot = intent.getParcelableExtra("btDevice");
        }

        gLView = new ChartGLSurfaceView(this);
        setContentView(gLView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (robot != null) { //if we are coming in from OpenGLActivity with an already found robot
            (new ConnectThread()).start();
        }

        gLView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeConnection();
        gLView.onPause(); //pause the rendering thread.
    }

    private void closeConnection() {
        try {
            if(robot != null) {
                robot = null;
            }
            if (robotConnection != null) {
                robotConnection.close();
            }
            if (fromRobot != null) {
                fromRobot.close();
            }
            if (read != null) {
                read.interrupt();
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
                fromRobot = new BufferedReader(new InputStreamReader(robotConnection.getInputStream()));
                read = new ReadThread();
                read.start();
            } catch (IOException e) {
                Log.e("Error", "connecting to robot error");
                e.printStackTrace();
            }
        }
    }

    private float[] createLeftArm(String[] p) {
        float[] toReturn = new float[12];
        int j = 0;

        for(int i = 9; i <= 15; i++) {
            toReturn[j] = Float.parseFloat(p[3*i]);
            toReturn[j + 1] = Float.parseFloat(p[3*i + 1]);
            toReturn[j + 2] = Float.parseFloat(p[3*i + 2]);
            j += 3;

            if(i == 9) {
                i += 3; //skip to joint 14
            }
        }
        return toReturn;
    }

    private float[] createLeftLeg(String[] p) {
        float[] toReturn = new float[15];
        int j = 0;
        for(int i = 0; i <= 8; i++) {
            toReturn[j] = Float.parseFloat(p[3 * i]);
            toReturn[j + 1] = Float.parseFloat(p[3 * i + 1]);
            toReturn[j + 2] = Float.parseFloat(p[3 * i + 2]);
            j += 3;
            if(i == 0) {
                i += 4; //skip to joint 6
            }
        }
        return toReturn;
    }

    private float[] createRightArm(String[] p) {
        float[] toReturn = new float[12];
        int j = 0;
        for(int i = 9; i <= 12; i++) {
            toReturn[j] = Float.parseFloat(p[3 * i]);
            toReturn[j + 1] = Float.parseFloat(p[3 * i + 1]);
            toReturn[j + 2] = Float.parseFloat(p[3 * i + 2]);
            j += 3;
        }
        return toReturn;
    }

    private float[] createRightLeg(String[] p) {
        float[] toReturn = new float[15];
        int j = 0;
        for(int i = 0; i <= 4; i++) {
            toReturn[j] = Float.parseFloat(p[3 * i]);
            toReturn[j + 1] = Float.parseFloat(p[3 * i + 1]);
            toReturn[j + 2] = Float.parseFloat(p[3 * i + 2]);
            j += 3;
        }
        return toReturn;
    }

    private float[] createSpine(String[] p) {
        float[] toReturn = new float[6];
        toReturn[0] = Float.parseFloat(p[0]);
        toReturn[1] = Float.parseFloat(p[1]);
        toReturn[2] = Float.parseFloat(p[2]);
        toReturn[3] = Float.parseFloat(p[27]);
        toReturn[4] = Float.parseFloat(p[28]);
        toReturn[5] = Float.parseFloat(p[29]);
        return toReturn;
    }
    private class ReadThread extends Thread { //Gets input from robot

        @Override
        public void run() {
            while(!isInterrupted()) {
                try {
                    String message = fromRobot.readLine(); //get message from robot
                    Log.i("info", "got " + message);
                    if (message != null && message.startsWith("DATA:")) { //if the message is not empty
                        message = message.substring(5);
                        String[] points = message.split(",");

                        float[] lA = createLeftArm(points);
                        float[] lL = createLeftLeg(points);
                        float[] rA = createRightArm(points);
                        float[] rL = createRightLeg(points);
                        float[] spine = createSpine(points);

                        float[] maxVal = {Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE};
                        float[] minVal = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};

                        for(int i = 0; i < 15; i++) {
                            float max = Float.MIN_VALUE;
                            float min = Float.MAX_VALUE;
                            if(i < lA.length) {
                                max = Math.max(lA[i], max);
                                min = Math.min(lA[i], min);
                            }
                            if(i < lL.length) {
                                max = Math.max(lL[i], max);
                                min = Math.min(lL[i], min);
                            }
                            if(i < rA.length) {
                                max = Math.max(rA[i], max);
                                min = Math.min(rA[i], min);
                            }
                            if(i < rL.length) {
                                max = Math.max(rL[i], max);
                                min = Math.min(rL[i], min);
                            }
                            if(i < spine.length) {
                                max = Math.max(spine[i], max);
                                min = Math.min(spine[i], min);
                            }
                            switch(i % 3) {
                                case 0:
                                    maxVal[0] = max;
                                    minVal[0] = min;
                                    break;
                                case 1:
                                    maxVal[1] = max;
                                    minVal[1] = min;
                                    break;
                                case 2:
                                    maxVal[2] = max;
                                    minVal[2] = min;
                            }
                        }
                        gLView.setSkeletonData(lA, lL, rA, rL, spine, maxVal, minVal);
                        customHandler.postDelayed(this, 0);
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