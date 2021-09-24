package com.example.bluetoothrobot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer renderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        //Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        renderer = new MyGLRenderer();

        //Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private float previousX;
    private float previousY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        float TOUCH_SCALE_FACTOR = 180.0f / 320;

        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = x - previousX;
            float dy = y - previousY;

            if (y > getHeight() / 2.0f) {
                dx = dx * -1;
            }
            if (x < getWidth() / 2.0f) {
                dy = dy * -1;
            }
            renderer.setAngle(
                    renderer.getAngle() + ((dx + dy) * TOUCH_SCALE_FACTOR));
            requestRender();
        }
        previousX = x;
        previousY = y;
        return true;
    }
}
