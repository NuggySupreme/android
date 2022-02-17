package com.example.bluetoothrobot;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SkeletonGLRenderer implements GLSurfaceView.Renderer {

    private Line lA;
    private Line lL;
    private Line rA;
    private Line rL;
    private Line spine;

    private final float[] vpMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private float[] rotationMatrix = new float[16];
    public volatile float mAngle;
    public float maxX = 1.0f;
    public float maxY = 1.0f;
    public float maxZ = 2.0f;
    public float minX = -1.0f;
    public float minY = -1.0f;
    public float minZ = 1.0f;
    public float cX = 1.0f;
    public float cY = 1.0f;
    Context context;

    /**
     * Constructor
     */
    public SkeletonGLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        float[] scratch = new float[16];

        //Autoscales the model to fit in the window if it is centered at the origin
        Matrix.frustumM(projectionMatrix, 0, minX - (0.1f * Math.abs(cX)), maxX + (0.1f * Math.abs(cX)), minY - (0.1f * Math.abs(cY)), maxY + (0.1f * Math.abs(cY)), 3, 7);

        //Create view and MVP matrix for drawing
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        long time = SystemClock.uptimeMillis() % 4000L;
        mAngle = 0.090f * ((int) time);
        Matrix.setRotateM(rotationMatrix, 0, mAngle, 0, 0, -1.0f);
        Matrix.multiplyMM(scratch, 0, vpMatrix, 0, rotationMatrix, 0);

        lA.draw(scratch);
        lL.draw(scratch);
        rA.draw(scratch);
        rL.draw(scratch);
        spine.draw(scratch);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        float ratio = (float) width/height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        lA = new Line(12);
        lL = new Line(12);
        rA = new Line(12);
        rL = new Line(12);
        spine = new Line(18);
    }

    public void setVertices(float[] min, float[] max, float[] la, float[] ll, float[] ra, float[] rl, float[] s) {
        maxX = max[0];
        maxY = max[1];
        maxZ = max[2];
        minX = min[0];
        minY = min[1];
        minZ = min[2];
        cX = max[0] - min[0];
        cY = max[1] - min[1];
        lA.setVertices(la);
        lL.setVertices(ll);
        rA.setVertices(ra);
        rL.setVertices(rl);
        spine.setVertices(s);
    }
}