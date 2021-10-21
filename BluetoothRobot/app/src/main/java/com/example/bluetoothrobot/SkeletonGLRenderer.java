package com.example.bluetoothrobot;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SkeletonGLRenderer implements GLSurfaceView.Renderer {

    //private final Skeleton skeleton = new Skeleton();
    private Line line;
    private Line lA;
    private Line lL;
    private Line rA;
    private Line rL;
    private Line spine;

    private final float[] vpMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    public float maxX = 1.0f;
    public float maxY = 1.0f;
    public float maxZ = 1.0f;
    public float minX = 0.0f;
    public float minY = 0.0f;
    public float minZ = 0.0f;
    public float cX = 0.5f;
    public float cY = 0.5f;

    public volatile float[] vertices = new float[15];

    int width;
    int height;
    Context context;

    /**
     * Constructor
     */
    public SkeletonGLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if(minZ < 0.1f) { //check if minZ is too close
            minZ = 0.1f;
        }
        //Autoscales the model to fit in the window if it is centered at the origin
        Matrix.frustumM(projectionMatrix, 0, minX - (0.1f * Math.abs(cX)), maxX + (0.1f * Math.abs(cX)), minY - (0.1f * Math.abs(cY)), maxY + (0.1f * Math.abs(cY)), 3, 7);

        //Create view and MVP matrix for drawing
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        line.draw(vpMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width/height;

        if(minZ < 0.1f) {
            minZ = 0.1f;
        }
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        line = new Line(15);
        lA = new Line(12);
        lL = new Line(15);
        rA = new Line(12);
        rL = new Line(15);
        spine = new Line(6);
        new ChangeThread().start();
    }

    public void setVertices(float[] v) {
        line.setVertices(v);
    }

    private class ChangeThread extends Thread {
        float[] v1 = {2000f, -5000f, 0f, 2000f, 900.0f, 0.0f, 2000.0f, 800.0f, 0.0f, 5000f, 0.0f, 0f, 5000f, 5000f, 0f};
        float[] v2 = {2000f, 5000f, 0f, 2000f, 400.0f, 0.0f, 2000.0f, 800.0f, 0.0f, 5000f, 805.0f, 0f, 5000f, -5000f, 0f};

        public void run() {
            int i = 0;
            while(!isInterrupted()) {
                try {
                    if (i == 0) {
                        setVertices(v1);
                        maxX = 5000.0f;
                        minX = 2000.0f;
                        cX = maxX - minX;
                        maxY = 5000.0f;
                        minY = -5000.0f;
                        cY = maxY - minY;
                        i = 1;
                    } else if (i == 1) {
                        setVertices(v2);
                        i = 0;
                    }
                    Thread.sleep(250);
                } catch(InterruptedException e) {
                    this.interrupt();
                    break;
                }
            }
        }
    }
    /* private class Skeleton {
        public float[] leftArm = new float[12];
        public float[] leftLeg = new float[15];
        public float[] rightArm = new float[12];
        public float[] rightLeg = new float[15];
        public float[] spine = new float[6];


        private float[] lAVert = new float[12];
        private float[] lLVert = new float[15];
        private float[] rAVert = new float[12];
        private float[] rLVert = new float[15];
        private float[] sVert = new float[6];

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

        public void drawSkeleton() {
            lAVert = leftArm.clone();
            lLVert = leftLeg.clone();
            rAVert = rightArm.clone();
            rLVert = rightLeg.clone();
            sVert = spine.clone();
        }

        public void vertexGenerate() {
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

        public void draw(GL10 gl) {
            //Log.d("Chart Ratio3 "," width " +width + " H " + height);
            gl.glViewport(0, 0, width, height);
            // bind the previously generated texture
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            // set the color for the triangle
            gl.glColor4f(0.2f, 0.2f, 0.2f, 0.5f);

            // Point to vertex buffer
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lABuffer);
            // Line width
            gl.glLineWidth(3.0f);
            // Draw the vertices as triangle strip
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, lAVert.length / 3);

            // Point to vertex buffer
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lLBuffer);
            // Line width
            gl.glLineWidth(3.0f);
            // Draw the vertices as triangle strip
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, lLVert.length / 3);

            // Point to vertex buffer
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, rABuffer);
            // Line width
            gl.glLineWidth(3.0f);
            // Draw the vertices as triangle strip
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, rAVert.length / 3);

            // Point to vertex buffer
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, rLBuffer);
            // Line width
            gl.glLineWidth(3.0f);
            // Draw the vertices as triangle strip
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, rLVert.length / 3);

            // Point to vertex buffer
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sBuffer);
            // Line width
            gl.glLineWidth(3.0f);
            // Draw the vertices as triangle strip
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, sVert.length / 3);

            //Disable the client state before leaving
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
    } */
}