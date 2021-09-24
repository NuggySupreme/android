package com.example.bluetoothrobot;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class OpenGLES20Activity extends AppCompatActivity {

    private MyGLSurfaceView gLView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        gLView = new MyGLSurfaceView(this);
        setContentView(gLView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //The following call pauses the rendering thread.
        //If your OpenGL application is memory intensive,
        //you should consider de-allocating objects that
        //consume significant memory here
        gLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //The following call resumes a paused rendering thread.
        //If you de-allocated graphic objects for onPause()
        //this is a good place to re-allocate them
        gLView.onResume();
    }
}