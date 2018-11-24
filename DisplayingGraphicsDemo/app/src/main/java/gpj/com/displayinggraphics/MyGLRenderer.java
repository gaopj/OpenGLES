package gpj.com.displayinggraphics;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static gpj.com.displayinggraphics.OpenGLES20Activity.TAG;


public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Triangle mTriangle;
    private Square   mSquare;

    @Override
    public void onDrawFrame(GL10 unused) {
        Log.d(TAG,"onDrawFrame");

        // 重绘背景颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        mTriangle.draw();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG,"onSurfaceCreated:"+config);

        // 设置背景框架颜色
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // 初始化一个三角形
        mTriangle = new Triangle();
        // 初始化一个正方形
        mSquare = new Square();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        Log.d(TAG,"onSurfaceChanged:width = "+width+",height = "+height);

        GLES20.glViewport(0, 0, width, height);

    }

    public static int loadShader(int type, String shaderCode){

        //创建顶点着色器类型（GLES20.GL_VERTEX_SHADER）
        //或片段着色器类型（GLES20.GL_FRAGMENT_SHADER）
        int shader = GLES20.glCreateShader(type);

        // 将源代码添加到着色器并进行编译
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}