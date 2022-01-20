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

public class SkeletonGLES20Activity extends AppCompatActivity {

    private SkeletonGLSurfaceView gLView;
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

        gLView = new SkeletonGLSurfaceView(this);
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
    private class ReadThread extends Thread { //Gets input from robot

        @Override
        public void run() {
            while(!isInterrupted()) {
                try {
                    String message = fromRobot.readLine(); //get message from robot
                    if (message.startsWith("DATA:")) { //if the message is not empt
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

                        System.out.println(maxVal[0]);
                        gLView.setSkeletonData(minVal, maxVal, lA, lL, rA, rL, spine);
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

    public static int max(int a, int b, int c, int d, int e) {
        int max = a;
        max = Math.max(max, b);
        max = Math.max(max, c);
        max = Math.max(max, d);
        max = Math.max(max, e);
        return max;
    }
}