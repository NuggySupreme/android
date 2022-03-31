package com.example.bluetoothrobot;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ChartGLRenderer implements GLSurfaceView.Renderer {

    private Line points;
    private Line axis;
    private final float[] vpMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private float[] rotationMatrix = new float[16];
    public volatile float mAngle;
    public float maxY = 1.0f;
    public float minY = -1.0f;
    public float cY = 0.0f;
    Context context;

    /**
     * Constructor
     */
    public ChartGLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        //float[] scratch = new float[16];

        //Autoscales the model to fit in the window if it is centered at the origin
        //Matrix.frustumM(projectionMatrix, 0, minX - (0.1f * Math.abs(cX)), maxX + (0.1f * Math.abs(cX)), minY - (0.1f * Math.abs(cY)), maxY + (0.1f * Math.abs(cY)), 3, 7);
        Matrix.orthoM(projectionMatrix, 0, -1.1f, 1.1f, minY - (0.1f * Math.abs(cY)), maxY + (0.1f * Math.abs(cY)), 3, 7);
        //Create view and MVP matrix for drawing
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        //long time = SystemClock.uptimeMillis() % 4000L;
        //mAngle = 0.090f * ((int) time);
        //Matrix.setRotateM(rotationMatrix, 0, mAngle, 0, 0, -1.0f);
        //Matrix.multiplyMM(scratch, 0, vpMatrix, 0, rotationMatrix, 0);

        points.draw(vpMatrix);
        axis.draw(vpMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        float ratio = (float) width/height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        points = new Line(33, 4.0f);
        axis = new Line(33, 2.0f);
        float[] vertices = new float[30];
        for(int i = 0; i < vertices.length; i+=3) {
            vertices[i] = -1.0f + 0.2f*i/3; //initialize x coordinates
        }
        points.setVertices(vertices);
        axis.setVertices(vertices);
    }

    public void addVertex(float val) {
        if(val > maxY) {
            maxY = val;
        }
        if(val < minY) {
            minY = val;
        }
        points.addYVertex(val);
        cY = maxY - minY;
        points.printVertices();
    }
}
