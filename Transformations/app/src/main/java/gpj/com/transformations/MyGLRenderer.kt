package gpj.com.transformations

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(view: GLSurfaceView) : GLSurfaceView.Renderer {


    private var mTriangle: Triangle? = null
    private var mView: GLSurfaceView? = null

    init {
        mView = view
    }

    override fun onDrawFrame(unused: GL10) {
        Log.d(TAG, "onDrawFrame")

        // 重绘背景颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)


        mTriangle!!.draw()


    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        Log.d(TAG, "onSurfaceCreated:$config")

        // 设置背景框架颜色
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // 初始化一个三角形
        mTriangle = Triangle(mView!!.context)

    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged:width = $width,height = $height")
        GLES20.glViewport(0, 0, width, height)

    }

    companion object {

        fun loadShader(type: Int, shaderCode: String): Int {

            //创建顶点着色器类型（GLES20.GL_VERTEX_SHADER）
            //或片段着色器类型（GLES20.GL_FRAGMENT_SHADER）
            val shader = GLES20.glCreateShader(type)

            // 将源代码添加到着色器并进行编译
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            return shader
        }
    }
}