package com.example.bluetoothrobot;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

public class ChartView extends GLSurfaceView {
    private ChartGLRenderer renderer;

    public ChartView(Context context) {
        super(context);

        setEGLContextClientVersion(3);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // Render the view only when there is a change in the drawing data
        // Set the Renderer for drawing on the GLSurfaceView
        renderer = new ChartGLRenderer(context);
        setRenderer(renderer);
    }

    public void addVertex(float val) {
        renderer.addVertex(val);
    }
}
