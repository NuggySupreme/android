package com.example.bluetoothrobot;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

import java.util.Arrays;

public class ChartGLSurfaceView extends GLSurfaceView {

    private final ChartGLRenderer chartRenderer;
    private float[] dataPoints = new float[100];

    boolean isUpdating = true; //flag for when the chart data points are updating

    public ChartGLSurfaceView(Context context) {
        super(context);

        //Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        chartRenderer = new ChartGLRenderer(this.getContext());

        //Set the Renderer for drawing on the GLSurfaceView
        setRenderer(chartRenderer);

        Arrays.fill(dataPoints, 0);

        appendChartData(0, 0);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        new Thread(new Task()).start();
    }

    public void appendChartData(float x, float y) {
        if(x != 0 || y != 0) {
            isUpdating = true;
            System.arraycopy(dataPoints, 2, this.dataPoints, 0, dataPoints.length - 2);
            this.dataPoints[this.dataPoints.length - 2] = x;
            this.dataPoints[this.dataPoints.length - 1] = y;
        }
        isUpdating = false;
    }

    class Task implements Runnable {
        @Override
        public void run() {
            while(true) {
                if(!isUpdating) {
                    chartRenderer.chartData = dataPoints;
                    requestRender();
                }
                try {
                    appendChartData((float)System.currentTimeMillis(), (float) Math.sin(System.currentTimeMillis()));
                    Thread.sleep(100);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
