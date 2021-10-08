package com.example.bluetoothrobot;


import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

public class ChartGLSurfaceView extends GLSurfaceView{

    private final ChartGLRenderer chartRenderer;
    private float[] datapoints = new float[150];

    boolean isUpdating = false;

    public ChartGLSurfaceView(Context context) {
        super(context);

        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // Set the Renderer for drawing on the GLSurfaceView
        chartRenderer = new ChartGLRenderer(context);
        setRenderer(chartRenderer);

        float j = -7.0f;
        for (int i = 0; i < datapoints.length; i+= 2){
            datapoints[i] = j;
            j += 0.2f;
        }

        setChartData(datapoints);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        new Thread(new Task()).start();
    }

    public void setChartData(float[] datapoints) {
        if (datapoints.length > 0){
            isUpdating = true;
            this.datapoints = datapoints.clone();
            isUpdating = false;
        }
    }

    public float[] getChartData() {
        return this.datapoints;
    }

    class Task implements Runnable {
        @Override
        public void run() {
            while (true){
                if (!isUpdating){
                    chartRenderer.chartData = datapoints;
                    requestRender();
                }

                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
