package gpj.com.hellotriangle


import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer {


    private var mTriangle: Triangle? = null

    override fun onDrawFrame(unused: GL10) {
        Log.d(TAG, "onDrawFrame")

        // 重绘背景颜色
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        mTriangle!!.draw()
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        Log.d(TAG, "onSurfaceCreated:$config")

        // 设置背景框架颜色
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // 初始化一个三角形
        mTriangle = Triangle()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged:width = $width,height = $height")
        GLES30.glViewport(0, 0, width, height)

    }

    companion object {

        fun loadShader(type: Int, shaderCode: String): Int {

            //创建顶点着色器类型（GLES20.GL_VERTEX_SHADER）
            //或片段着色器类型（GLES20.GL_FRAGMENT_SHADER）
            val shader = GLES30.glCreateShader(type)

            // 将源代码添加到着色器并进行编译
            GLES30.glShaderSource(shader, shaderCode)
            GLES30.glCompileShader(shader)

            return shader
        }
    }
}