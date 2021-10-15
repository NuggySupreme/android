package com.example.kobukirobot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class JoystickView extends SurfaceView implements SurfaceHolder.Callback {

    private float centerX;
    private float centerY;
    private float baseRadius;
    private float hatRadius;

    public JoystickView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        setupDimensions();
        drawJoystick(centerX, centerY);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void drawJoystick(float newX, float newY) {
        if(getHolder().getSurface().isValid()) {
            Canvas myCanvas = this.getHolder().lockCanvas();
            Paint colors = new Paint();
            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //Clear background
            colors.setARGB(255, 50, 50, 50);
            myCanvas.drawCircle(centerX, centerY, baseRadius, colors);
            colors.setARGB(255, 0, 0, 255);
            myCanvas.drawCircle(newX, newY, hatRadius, colors);
            getHolder().unlockCanvasAndPost(myCanvas);
        }
    }

    private void setupDimensions() {
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        baseRadius = Math.min(getWidth(), getHeight()) / 3;
        hatRadius = Math.min(getWidth(), getHeight()) / 5;
    }
}