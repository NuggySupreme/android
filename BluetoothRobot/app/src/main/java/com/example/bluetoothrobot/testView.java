package com.example.bluetoothrobot;

import android.content.Context;
import android.opengl.GLSurfaceView;

class TestView extends GLSurfaceView {

    private final TestRenderer renderer;

    public TestView(Context context){
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        renderer = new TestRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}