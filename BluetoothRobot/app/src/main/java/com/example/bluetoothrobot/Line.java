package com.example.bluetoothrobot;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Line {
    private FloatBuffer vertexBuffer;
    private float[] vertices = {-1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f}; //L/R U/D I/O
    private float[] color = {0.0f, 0.0f, 0.0f, 1.0f};
    private float lineWidth = 8.0f;
    private final String vertexShaderCode =
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

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public Line() {
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

    public Line(int length) {
        vertices = new float[length];
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
    public Line(int length, float width) {
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

    public void setVertices(float[] v) {
        vertices = v.clone();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
    }

    public void addYVertex(float v) {
        for(int i = 4; i < vertices.length; i += 3) {
            vertices[i - 3] = vertices[i];
        }
    }

    public void printVertices() {
        System.out.println("points");
        for(int i = 0; i < vertices.length; i+=3) {
            System.out.print("("+vertices[i]+","+vertices[i+1]+","+vertices[i+2]+") ");
        }
        System.out.println();
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(shaderProgram);


        int positionAttrib = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionAttrib);


        GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer); //draw vertices

        int colorUniform = GLES20.glGetUniformLocation(shaderProgram, "vColor");

        GLES20.glUniform4fv(colorUniform, 1, color, 0);
        GLES20.glLineWidth(lineWidth);

        vpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(vpMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, vertices.length / 3);
        GLES20.glDisableVertexAttribArray(positionAttrib);
    }
}
