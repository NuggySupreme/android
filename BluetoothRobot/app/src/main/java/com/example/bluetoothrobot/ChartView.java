package com.example.bluetoothrobot;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

public class ChartView extends GLSurfaceView {
    private ChartGLRenderer renderer; //renderer for chart

    public ChartView(Context context) {
        super(context);

        setEGLContextClientVersion(3); //use openGLES 3.0
        setEGLConfigChooser(8, 8, 8, 8, 16, 0); //use 8-bit colors
        this.setZOrderOnTop(true); //this view will always appear on top of other elements in the same area of the screen
        getHolder().setFormat(PixelFormat.TRANSLUCENT); //no idea what this does

        // Render the view continuously
        // Set the Renderer for drawing on the GLSurfaceView
        renderer = new ChartGLRenderer(context);
        setRenderer(renderer);
    }

    public void addVertex(float val) {
        renderer.addVertex(val);
    } //add new point to chart
}
