package gpj.com.displayinggraphics;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Triangle {

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private FloatBuffer vertexBuffer;
    private int mProgram;

    // 此数组中每个顶点的维度
    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = {   // 按逆时针顺序
            0.0f,  0.622008459f, 0.0f, // 上
            -0.5f, -0.311004243f, 0.0f, // 左下
            0.5f, -0.311004243f, 0.0f  // 右下
    };

    // 设置颜色的R（红）,G（绿）,B（蓝）,A（透明度） 值
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    public Triangle() {
        // 为形状坐标数组初始化顶点的字节缓冲区
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# squareCoords 数组长度 * 每个float占4字节)
                triangleCoords.length * 4);

        // 缓冲区读取顺序使用设备硬件的本地字节读取顺序
        bb.order(ByteOrder.nativeOrder());

        // 从ByteBuffer创建一个浮点缓冲区
        vertexBuffer = bb.asFloatBuffer();
        // 将坐标点加到FloatBuffer中
        vertexBuffer.put(triangleCoords);
        // 设置缓冲区开始读取位置，这边设置为从头开始读取
        vertexBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // 创建一个空的OpenGL ES 项目
        mProgram = GLES20.glCreateProgram();

        // 将顶点着色器添加到程序中
        GLES20.glAttachShader(mProgram, vertexShader);

        // 将片段着色器添加到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);

        // 创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(mProgram);
    }

    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; //一个顶点占用空间，其中每个顶点单维值占4字节

    public void draw() {
        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(mProgram);

        // 获取顶点着色器vPosition属性（位置）的句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // 启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // 准备三角坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // 获取片段着色器vColor成员（颜色）的句柄
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // 禁用顶点数组
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}