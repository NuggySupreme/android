package com.example.bluetoothrobot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

public class ChartGLRenderer implements GLSurfaceView.Renderer {

    private final Skeleton skeleton = new Skeleton();

    public volatile float[] leftArmData = new float[12];
    public volatile float[] leftLegData = new float[15];
    public volatile float[] rightArmData = new float[12];
    public volatile float[] rightLegData = new float[15];
    public volatile float[] spineData = new float[6];

    public float maxX = 0.0f;
    public float maxY = 0.0f;
    public float maxZ = 0.0f;
    public float minX = 0.0f;
    public float minY = 0.0f;
    public float minZ = 0.0f;
    int width;
    int height;
    Context context;

    /** Constructor */
    public ChartGLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // clear Screen and Depth Buffer
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        // Reset the Modelview Matrix
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        // Drawing
        //Log.d("Chart Ratio1 "," width " +width + " H " + height);


        skeleton.setResolution(width, height);
        skeleton.setSkeletonData(leftArmData, leftLegData, rightArmData, rightLegData, spineData);
        skeleton.draw(gl);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;

        if(height == 0) {                       //Prevent A Divide By Zero By
            height = 1;                         //Making Height Equal One
        }
        gl.glViewport(0, 0, width, height);     //Reset The Current Viewport
        gl.glMatrixMode(gl.GL_PROJECTION);    //Select The Projection Matrix
        gl.glLoadIdentity();                    //Reset The Projection Matrix
        GLU.gluPerspective();
        //Calculate The Aspect Ratio Of The Window
        //Log.d("Chart Ratio2 "," width " +width + " H " + height);
        gl.glMatrixMode(GL10.GL_MODELVIEW);     //Select The Modelview Matrix
        gl.glLoadIdentity();                    //Reset The Modelview Matrix
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}

class Skeleton {
    public float[] leftArm = new float[12];
    public float[] leftLeg = new float[15];
    public float[] rightArm = new float[12];
    public float[] rightLeg = new float[15];
    public float[] spine = new float[6];


    private float[] lAVert = new float[12];
    private float[] lLVert = new float[15];
    private float[] rAVert = new float[12];
    private float[] rLVert = new float[15];
    private float[] sVert = new float[12];

    private FloatBuffer lABuffer;
    private FloatBuffer lLBuffer;
    private FloatBuffer rABuffer;
    private FloatBuffer rLBuffer;
    private FloatBuffer sBuffer;

    int width;
    int height;

    public Skeleton() {
        drawSkeleton();
        vertexGenerate();
    }

    public void setSkeletonData(float[] lA, float[] lL, float[] rA, float[] rL, float[] s) {
        this.leftArm = lA;
        this.leftLeg = lL;
        this.rightArm = rA;
        this.rightLeg = rL;
        this.spine = s;
        drawSkeleton();
        vertexGenerate();
    }

    public void drawSkeleton (){
        lAVert = leftArm.clone();
        lLVert = leftLeg.clone();
        rAVert = rightArm.clone();
        rLVert = rightLeg.clone();
        sVert = spine.clone();
    }

    public void vertexGenerate(){
        // a float has 4 bytes so we allocate for each coordinate 4 bytes
        ByteBuffer lAByteBuffer = ByteBuffer.allocateDirect(lAVert.length * 4);
        ByteBuffer lLByteBuffer = ByteBuffer.allocateDirect(lLVert.length * 4);
        ByteBuffer rAByteBuffer = ByteBuffer.allocateDirect(rAVert.length * 4);
        ByteBuffer rLByteBuffer = ByteBuffer.allocateDirect(rLVert.length * 4);
        ByteBuffer sByteBuffer = ByteBuffer.allocateDirect(sVert.length * 4);

        lAByteBuffer.order(ByteOrder.nativeOrder());
        lLByteBuffer.order(ByteOrder.nativeOrder());
        rAByteBuffer.order(ByteOrder.nativeOrder());
        rLByteBuffer.order(ByteOrder.nativeOrder());
        sByteBuffer.order(ByteOrder.nativeOrder());

        // allocates the memory from the byte buffer
        lABuffer = lAByteBuffer.asFloatBuffer();
        lLBuffer = lLByteBuffer.asFloatBuffer();
        rABuffer = rAByteBuffer.asFloatBuffer();
        rLBuffer = rLByteBuffer.asFloatBuffer();
        sBuffer = sByteBuffer.asFloatBuffer();

        // fill the vertexBuffer with the vertices
        lABuffer.put(lAVert);
        lLBuffer.put(lLVert);
        rABuffer.put(rAVert);
        rLBuffer.put(rLVert);
        sBuffer.put(sVert);

        // set the cursor position to the beginning of the buffer
        lABuffer.position(0);
        lLBuffer.position(0);
        rABuffer.position(0);
        rLBuffer.position(0);
        sBuffer.position(0);
    }

    public void setResolution(int width, int height){
        this.width = width;
        this.height = height;
    }

    public void draw(GL10 gl) {
        //Log.d("Chart Ratio3 "," width " +width + " H " + height);
        gl.glViewport(0, 0, width, height);
        // bind the previously generated texture
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // set the color for the triangle
        gl.glColor4f(0.2f, 0.2f, 0.2f, 0.5f);

        // Point to vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0,lABuffer);
        // Line width
        gl.glLineWidth(3.0f);
        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, lAVert.length/3);

        // Point to vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lLBuffer);
        // Line width
        gl.glLineWidth(3.0f);
        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, lLVert.length/3);

        // Point to vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, rABuffer);
        // Line width
        gl.glLineWidth(3.0f);
        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, rAVert.length/3);

        // Point to vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, rLBuffer);
        // Line width
        gl.glLineWidth(3.0f);
        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, rLVert.length/3);

        // Point to vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sBuffer);
        // Line width
        gl.glLineWidth(3.0f);
        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, sVert.length/3);

        //Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
}