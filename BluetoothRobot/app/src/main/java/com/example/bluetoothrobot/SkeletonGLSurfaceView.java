package com.example.bluetoothrobot;


import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

public class SkeletonGLSurfaceView extends GLSurfaceView{

    private SkeletonGLRenderer skeletonRenderer;

    public SkeletonGLSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(3);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // Render the view only when there is a change in the drawing data
        // Set the Renderer for drawing on the GLSurfaceView
        skeletonRenderer = new SkeletonGLRenderer(context);
        setRenderer(skeletonRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void setSkeletonData(float[] minVal, float[] maxVal, float[] la, float[] ll, float[] ra, float[] rl, float[] s) {
        skeletonRenderer.setVertices(minVal, maxVal, la, ll, ra, rl, s);
    }
}
