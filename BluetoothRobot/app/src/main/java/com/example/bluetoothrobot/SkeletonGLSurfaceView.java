package com.example.bluetoothrobot;


import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class SkeletonGLSurfaceView extends GLSurfaceView{

    private SkeletonGLRenderer skeletonRenderer;

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float previousX;
    private float previousY;
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - previousX;
                float dy = y - previousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1 ;
                }

                skeletonRenderer.setXAngle(
                        skeletonRenderer.getXAngle() +
                                (dx * TOUCH_SCALE_FACTOR));
                skeletonRenderer.setYAngle(
                        skeletonRenderer.getYAngle() +
                                (dy * TOUCH_SCALE_FACTOR));
                requestRender();
        }

        previousX = x;
        previousY = y;
        return true;
    }

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
