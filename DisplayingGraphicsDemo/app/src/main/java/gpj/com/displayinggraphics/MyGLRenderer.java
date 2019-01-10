package gpj.com.displayinggraphics;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static gpj.com.displayinggraphics.OpenGLES20Activity.TAG;


public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Triangle mTriangle;
    private Square   mSquare;

    private float[] mRotationMatrix = new float[16];


    @Override
    public void onDrawFrame(GL10 unused) {
        Log.d(TAG,"onDrawFrame");

        float[] scratch = new float[16];

        // 重绘背景颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 设置相机的位置 (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // 计算投影和视图转换
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // 给三角形创建一个旋转变换
//         long time = SystemClock.uptimeMillis() % 4000L;
//         float angle = 0.090f * ((int) time);
//        Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);


        //将旋转矩阵与投影和摄像机视图结合时，mMVPMatrix因子必须在*前面才能使矩阵乘法乘积正确。
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        //mTriangle.draw(mMVPMatrix);
        mTriangle.draw(scratch);
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

        float ratio = (float) width / height;

        // 此投影矩阵应用于onDrawFrame（）方法中的对象坐标
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);


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

    // mMVPMatrix是“模型视图投影矩阵”(Model View Projection Matrix)的缩写
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];


    public volatile float mAngle;

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }
}