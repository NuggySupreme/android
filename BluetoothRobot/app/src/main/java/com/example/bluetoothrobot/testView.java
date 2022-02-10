package com.example.bluetoothrobot;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

class TestView extends GLSurfaceView {
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float previousX;
    private float previousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        switch(e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx =  x - previousX;
                float dy = y - previousY;
                if(y > getHeight() / 2) {
                    dx = dx * -1;
                }
                if(x < getWidth() / 2) {
                    dy = dy * - 1;
                }
                renderer.setAngle(renderer.getAngle() + ((dx + dy) * TOUCH_SCALE_FACTOR));
                requestRender();;
        }
        previousX = x;
        previousY = y;
        return true;
    }
    private final testRenderer renderer;

    public TestView(Context context){
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        renderer = new testRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}