package com.example.bluetoothrobot;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ChartGLRenderer implements GLSurfaceView.Renderer {

    private Line points; //xyz coordinates for the data to be graphed
    private Line axis; //A line from (-1, 0, 0) to (1, 0, 0) to serve as an axis
    private final float[] vpMatrix = new float[16]; //multiplied view-projection matrix for openGL rendering
    private final float[] projectionMatrix = new float[16]; //projection matrix for openGL rendering
    private final float[] viewMatrix = new float[16]; //view matrix for opengl rendering
    public float maxY = 1.0f; //maxY value since this changes in the chart
    public float minY = -1.0f; //minY value
    public float cY = 0.0f; //distance between min and max Y values
    Context context;

    /**
     * Constructor
     */
    public ChartGLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onDrawFrame(GL10 gl) { //onRequestRender() or new frame
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f); //reset background colors
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);


        //Autoscales the model to fit in the window if it is centered at the origin
        Matrix.orthoM(projectionMatrix, 0, -1.1f, 1.1f, minY - (0.1f * Math.abs(cY)), maxY + (0.1f * Math.abs(cY)), 3, 7);
        //Create view and MVP matrix for drawing
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        points.draw(vpMatrix); //draw lines in model space
        axis.draw(vpMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) { //usually when screen is rotated
        GLES30.glViewport(0, 0, width, height);
        float ratio = (float) width/height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) { //on initialization
        points = new Line(33, 4.0f); //create two 11 point line with each point having x,y,z coordinates
        axis = new Line(33, 2.0f); //
        float[] vertices = new float[30]; //set appropriate x-values for chart stuff
        for(int i = 0; i < vertices.length; i+=3) {
            vertices[i] = -1.0f + 0.2f*i/3; //initialize x coordinates
        }
        points.setVertices(vertices); //send the vertices to the drawing thread
        axis.setVertices(vertices);
    }

    public void addVertex(float val) {
        points.addYVertex(val); //add new Y point to data
        float[] chartData = points.getVertices(); //get the chartData and figure out the new min and max Y values
        for(int i = 1; i < chartData.length; i+=3) {
            maxY = Math.max(maxY, chartData[i]);
            minY = Math.min(minY, chartData[i]);
        }
        cY = maxY - minY; //update distance between them
        //points.printVertices(); print vertices in Logcat info window for debugging
    }
}
