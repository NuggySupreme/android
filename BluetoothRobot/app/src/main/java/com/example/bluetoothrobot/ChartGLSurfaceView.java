package com.example.bluetoothrobot;


import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class ChartGLSurfaceView extends GLSurfaceView{

    private final ChartGLRenderer chartRenderer;
    private float[] datapoints = new float[150];

    private float[] leftArm = new float[12];
    private float[] rightArm = new float[12];
    private float[] leftLeg =  new float[12];
    private float[] rightLeg = new float[12];
    private float[] spine = new float[6];

    boolean isUpdating = false;
    public RenderThread requestRender;

    public ChartGLSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        float j = -7.0f;
        for (int i = 0; i < datapoints.length; i+= 2){
            datapoints[i] = j;
            j += 0.2f;
        }

        setChartData(datapoints);

        // Render the view only when there is a change in the drawing data
        // Set the Renderer for drawing on the GLSurfaceView
        requestRender = new RenderThread();
        requestRender.start();

        chartRenderer = new ChartGLRenderer(context);
        setRenderer(chartRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void setChartData(float[] datapoints) {
        if (datapoints.length > 0){
            isUpdating = true;
            this.datapoints = datapoints.clone();
            isUpdating = false;
        }
    }

    public void setSkeletonData(float[] la, float[] ll, float[] ra, float[] rl, float[] spine) {
        if (la.length * ll.length * ra.length * rl.length > 0){
            isUpdating = true;
            this.leftArm = la.clone();
            this.rightArm = ra.clone();
            this.leftLeg = ll.clone();
            this.rightLeg = rl.clone();
            this.spine = spine.clone();
            isUpdating = false;
            Log.i("info", "updated skeleton info");
        }
    }

    class RenderThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()){
                if (!isUpdating){
                    chartRenderer.chartData = datapoints;
                    requestRender();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i("info", "render thread closed");
        }
    }
}
