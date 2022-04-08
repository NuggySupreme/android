package com.example.bluetoothrobot;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Line {
    private FloatBuffer vertexBuffer;
    private float[] vertices = {-1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f}; //L/R U/D I/O
    private float[] color = {0.0f, 0.0f, 0.0f, 1.0f}; //fully opaque black line
    private float lineWidth = 8.0f;
    private final String vertexShaderCode = //DON'T CHANGE THESE STRINGS
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
                "gl_Position = uMVPMatrix * vPosition;" +
            "}";
    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
                "gl_FragColor = vColor;" +
            "}";
    private int shaderProgram;
    private int vpMatrixHandle;

    public static int loadShader(int type, String shaderCode) { //this is part of the drawing stuff
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public Line() { //default constructor (currently not used)
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer(); //turn into float buffer
        vertexBuffer.put(vertices); //put vertices into buffer
        vertexBuffer.position(0); //read from beginning

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);
    }

    public Line(int length) { //create a line with (length) coordinates. # of (X, Y, Z) points in line = length / 3
        vertices = new float[length]; //create new array
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4); //allocate memory for array
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer(); //turn into float buffer
        vertexBuffer.put(vertices); //put vertices into buffer
        vertexBuffer.position(0); //read from beginning

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);
    }
    public Line(int length, float width) { //create new line of length/3 points and different line width
        vertices = new float[length];
        lineWidth = width;
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer(); //turn into float buffer
        vertexBuffer.put(vertices); //put vertices into buffer
        vertexBuffer.position(0); //read from beginning

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);
    }

    public void setVertices(float[] v) { //send vertices to be drawn
        vertices = v.clone(); //clone array to different memory location
        vertexBuffer.put(vertices); //put vertices into buffer and draw
        vertexBuffer.position(0);
    }

    public void addYVertex(float v) { //add new Y vertex to chart. Use i = 3 to shift X coordinates and i = 5 to shift Z coordinates
        for(int i = 4; i < vertices.length; i += 3) { //shift Y coordinates down the array
            vertices[i - 3] = vertices[i];
        }
        vertices[vertices.length - 2] = v; //last Y coordinate in line is set to new point
        setVertices(vertices);
    }

    public float[] getVertices() {
        return vertices;
    }

    public void printVertices() { //print out line vertices for debugging
        System.out.println("points");
        for(int i = 0; i < vertices.length; i+=3) {
            System.out.print("("+vertices[i]+","+vertices[i+1]+","+vertices[i+2]+") ");
        }
        System.out.println();
    }

    public void draw(float[] mvpMatrix) { //draw line
        GLES20.glUseProgram(shaderProgram);


        int positionAttrib = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionAttrib);


        GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer); //draw vertices

        int colorUniform = GLES20.glGetUniformLocation(shaderProgram, "vColor");

        GLES20.glUniform4fv(colorUniform, 1, color, 0);
        GLES20.glLineWidth(lineWidth);

        vpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(vpMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, vertices.length / 3); //line strips are for solid lines. triangles can be used for shapes
        GLES20.glDisableVertexAttribArray(positionAttrib);
    }
}
