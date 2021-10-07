package com.example.bluetoothrobot;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class OpenGLES20Activity extends AppCompatActivity {

    private ChartGLSurfaceView gLView;
    private final Handler customHandler = new Handler();
    private BluetoothSocket robotConnection;
    private BufferedReader fromRobot; //gets input from robot
    private ReadThread read = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        gLView = new ChartGLSurfaceView(this);
        setContentView(gLView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //The following call pauses the rendering thread.
        //If your OpenGL application is memory intensive,
        //you should consider de-allocating objects that
        //consume significant memory here
        read.go = false;
        gLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        robotConnection = Robot.getRobot();
        if(robotConnection != null) {
            try {
                fromRobot = new BufferedReader(new InputStreamReader(robotConnection.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        (read = new ReadThread()).start();
        //updateTimerThread.run();

        //The following call resumes a paused rendering thread.
        //If you de-allocated graphic objects for onPause()
        //this is a good place to re-allocate them
        gLView.onResume();
    }


    /*private final Runnable updateTimerThread = new Runnable() { //get information into GLChart
        public void run() {
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            float[] data = gLView.getChartData();
            for (int i = 1; i < data.length - 2; i += 2) {
                data[i] = data[i + 2];
            }
            float time = (float) (new Random().nextFloat() * 2 * Math.PI);
            data[data.length - 1] = (float) Math.sin(time);
            gLView.setChartData(data);

            customHandler.postDelayed(this, 0);
        }
    };*/

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