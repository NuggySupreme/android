package com.example.researchrobot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private Thread Thread1 = null; //thread to send connection request to robot
    private EditText etIP, etMessage; //Input field
    private TextView tvMessages; //TextView for messages from robot
    private String SERVER_IP; //Robot IP address
    private PrintWriter toRobot;
    private BufferedReader fromRobot;
    private Socket robotConnection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup UI elements
        etIP = findViewById(R.id.etIP);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);
        Button btnConnect = findViewById(R.id.btnConnect);

        //Set click event listeners for buttons
        btnConnect.setOnClickListener(v -> {
            tvMessages.setText(""); //clear message TextView
            SERVER_IP = etIP.getText().toString().trim(); //Get robot IP address and remove leading and trailing whitespace
            Thread1 = new Thread(new Thread1()); //Start a new connection to the robot
            Thread1.start();
        });

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim(); //get message to send to robot and cleanup leading and trailing whitespace
            if(!message.isEmpty()) {
                new Thread(new Thread3(message)).start(); //start thread to send message to robot
            }
        });
    }

    private class Thread1 implements Runnable { //Sends a TCP connection to the robot
        public void run() {
            if(!robotConnection.isConnected()) { //if there isn't already a connection to the robot
                try {
                    robotConnection = new Socket(SERVER_IP, R.integer.SERVER_PORT); //Send connection request to robot
                    toRobot = new PrintWriter(robotConnection.getOutputStream(), true); //Get output stream to send stuff to robot and set it to auto flush data
                    fromRobot = new BufferedReader(new InputStreamReader(robotConnection.getInputStream())); //Get input stream to get stuff from robot
                    //Update UI element with text from input stream
                    runOnUiThread(() -> tvMessages.setText("Connected\n"));
                    new Thread(new Thread2()).start(); //start a thread to handle input from robot
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Thread2 implements Runnable { //Gets input from robot
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
                    Thread1 = new Thread(new Thread1());
                    Thread1.start();
                    return;
                }
            }
        }
    }

    private class Thread3 implements Runnable {
        private final String message;
        Thread3(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            toRobot.write(message);
            runOnUiThread(() -> {
                tvMessages.append("android: " + message + "\n");
                etMessage.setText("");
            });
        }
    }
}