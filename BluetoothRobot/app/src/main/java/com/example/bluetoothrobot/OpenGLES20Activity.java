package com.example.bluetoothrobot;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import javax.microedition.khronos.egl.EGLConfig;

import android.os.Bundle;

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
}