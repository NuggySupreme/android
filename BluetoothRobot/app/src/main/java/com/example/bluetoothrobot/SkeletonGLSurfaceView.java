package com.example.bluetoothrobot;


import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class SkeletonGLSurfaceView extends GLSurfaceView{

    private SkeletonGLRenderer skeletonRenderer;

    private float[] leftArm = new float[12];
    private float[] rightArm = new float[12];
    private float[] leftLeg =  new float[15];
    private float[] rightLeg = new float[15];
    private float[] spine = new float[6];
    private float[] maxVal = new float[3];
    private float[] minVal = new float[3];

    boolean isUpdating = false;

    public SkeletonGLSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // Render the view only when there is a change in the drawing data
        // Set the Renderer for drawing on the GLSurfaceView
        skeletonRenderer = new SkeletonGLRenderer(context);
        setRenderer(skeletonRenderer);
    }

    public void setSkeletonData(float[] la, float[] ll, float[] ra, float[] rl, float[] spine, float[] maxVal, float[] minVal) {
        if (la.length * ll.length * ra.length * rl.length > 0){
            isUpdating = true;
            this.leftArm = la.clone();
            this.rightArm = ra.clone();
            this.leftLeg = ll.clone();
            this.rightLeg = rl.clone();
            this.spine = spine.clone();
            this.maxVal = maxVal;
            this.minVal = minVal;
            isUpdating = false;
        }
    }
}
