package com.example.kobukirobot;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private Socket robot = null; //TCP/IP socket for communication
    private BufferedReader fromRobot = null; //input stream reader for socket
    private ReadThread read = null; //reading thread to prevent UI blocking
    private PrintWriter toRobot = null; //output stream for socket
    private TextView etSensors; //message log

    private boolean joystickResting = false;
    private boolean connected = false;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSensors = findViewById(R.id.sensorPanel);
        EditText etMessage = findViewById(R.id.msg);
        btnSend = findViewById(R.id.btnSend);
        Button btnClose = findViewById(R.id.btnClose);

        btnSend.setOnClickListener(v -> { //Send message to robot
            String message = etMessage.getText().toString().trim(); //get message to send to robot and cleanup leading and trailing whitespace
            if(!connected) {
                (new ConnectThread(message)).start();
            } else {
                if (toRobot != null) {
                    if (!message.isEmpty()) { //If there is a message to send
                        (new WriteThread(message)).start(); //start thread to send message to robot
                    }
                } else {
                    Snackbar.make(findViewById(R.id.activity_main), "Please connect to the robot", Snackbar.LENGTH_LONG).show();
                }
            }
            etMessage.setText("");
            etSensors.setText("");
        });

        btnClose.setOnClickListener(v -> closeConnection());

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                String message = angle + "," + strength;
                if(toRobot != null) {
                    if(angle == 0 && strength == 0 && !joystickResting) { //joystick just returned to resting position. must tell robot to stop
                        (new WriteThread(message)).start();
                        joystickResting = true; //set resting flag
                    } else if(angle != 0 || strength != 0) { //if joystick is not resting, send data
                        (new WriteThread(message)).start();
                        joystickResting = false;
                    }
                } else { //connection unavailable
                    Log.e("error", "didn't create write thread. data incompatible");

                }
            }
        });
    }

    @Override
    protected void onDestroy() { //Cleanup on app close
        super.onDestroy();
        closeConnection();
    }

    private void closeConnection() {
        try {
            if(robot != null) {
                robot.close();
            }
            if(toRobot != null) {
                toRobot.close();
            }
            if(fromRobot != null) {
                fromRobot.close();
            }
            connected = false;
            runOnUiThread(() -> {
                btnSend.setText("CONNECT");
                etSensors.setText("");
            });

        } catch(Exception e) {
            Log.e("Error", "connection closing error");
            e.printStackTrace();
        }
    }

    private synchronized void write(String message) {
        toRobot.write(message);
        toRobot.flush();
    }

    private class ConnectThread extends Thread { //Sends a bluetooth connection to the robot
        private String server = null;

        public ConnectThread(String server) {
            this.server = server;
        }

        public void run() {
            try {
                robot = new Socket(server, 8080);
                Snackbar.make(findViewById(R.id.activity_main), "Connected to robot", Snackbar.LENGTH_LONG).show(); //Notify the user that the two devices are connecte
                toRobot = new PrintWriter(robot.getOutputStream()); //Get output stream to send stuff to robot
                fromRobot = new BufferedReader(new InputStreamReader(robot.getInputStream()));
                read = new ReadThread();
                read.start();
                runOnUiThread(() -> {
                    btnSend.setText("SEND");
                });
                connected = true;
            } catch (IOException e) {
                Log.e("Error", "connecting to robot error");
                connected = false;
                e.printStackTrace();
            }
        }
    }

    private class ReadThread extends Thread { //Gets input from robot
        @Override
        public void run() {
            while(!isInterrupted()) {
                try {
                    String message = fromRobot.readLine();
                    final String temp = message;
                    if (!temp.equals("")) {
                        runOnUiThread(() -> {
                            etSensors.append(temp + '\n');
                        });
                    }
                } catch (IOException e) {
                    Log.e("error", "reading from robot error");
                    connected = false;
                    runOnUiThread(() -> {
                        btnSend.setText("CONNECT");
                    });
                    read.interrupt();
                    e.printStackTrace();
                }
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
            try {
                write(message);
            } catch(Exception e) {
                connected = false;
                runOnUiThread(() -> {
                    btnSend.setText("CONNECT");
                });
                Log.e("error", "didn't send message");
            }
        }
    }
}