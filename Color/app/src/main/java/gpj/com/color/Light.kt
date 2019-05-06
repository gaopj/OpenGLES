package gpj.com.color


import android.content.Context
import android.opengl.GLES20.GL_MAX_VERTEX_ATTRIBS
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import java.nio.FloatBuffer
import java.nio.IntBuffer


class Light(context: Context) {

    private val mContext: Context

    private val vertexShaderCode =
            "#version 300 es \n" +

                    " layout (location = 0) in vec3 aPos;" +

                    "uniform mat4 model;" +
                    "uniform mat4 view;" +
                    "uniform mat4 projection;" +

                    "void main() {" +
                    " gl_Position = projection * view * model * vec4(aPos, 1.0);" +

                    "}"


    private val fragmentShaderCode = (
            "#version 300 es \n " +
                    "#ifdef GL_ES\n" +
                    "precision highp float;\n" +
                    "#endif\n" +

                    "out vec4 FragColor; " +

                    "void main() {" +
                    "  FragColor = vec4(1.0) ;" +
                    "}")


    private val mProgram: Int

    private val VBOids: IntBuffer
    private val VAOids: IntBuffer
    private val EBOids: IntBuffer

    private val mModelMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)


    var mLightColor: Array<Float> =  arrayOf(1f, 1f, 1f)
    var mLightPosition: Array<Float> = arrayOf(0.2f,0.3f, -1f)
   // var mLightView: Array<Float> = arrayOf(0.1f, 0.1f, 4.5f)



    init {
        mContext = context.applicationContext
        // 查询Attribute变量允许个数
        var maxVertexAttribute = IntBuffer.allocate(1)
        GLES30.glGetIntegerv(GL_MAX_VERTEX_ATTRIBS, maxVertexAttribute)
        Log.d(TAG, "maxVertexAttribute:" + maxVertexAttribute.get(0))

        VBOids = IntBuffer.allocate(1);
        GLES30.glGenBuffers(1, VBOids)
        Log.d(TAG, "VBO:" + VBOids.get(0))

        VAOids = IntBuffer.allocate(1);
        GLES30.glGenBuffers(1, VAOids)

        EBOids = IntBuffer.allocate(1);
        GLES30.glGenBuffers(1, EBOids)


        var vertexShader = MyGLRenderer.loadShader(GLES30.GL_VERTEX_SHADER,
                vertexShaderCode)
        var success: IntBuffer = IntBuffer.allocate(1)
        GLES30.glGetShaderiv(vertexShader, GLES30.GL_COMPILE_STATUS, success)

        if (success.get(0) == 0) {
            Log.e(TAG, GLES30.glGetShaderInfoLog(vertexShader));
            GLES30.glDeleteShader(vertexShader);
            vertexShader = 0
        }


        var fragmentShader = MyGLRenderer.loadShader(GLES30.GL_FRAGMENT_SHADER,
                fragmentShaderCode)
        GLES30.glGetShaderiv(fragmentShader, GLES30.GL_COMPILE_STATUS, success)
        if (success.get(0) == 0) {

            Log.e(TAG, GLES30.glGetShaderInfoLog(fragmentShader))
            GLES30.glDeleteShader(fragmentShader)
            fragmentShader = 0
        }

        // 创建一个空的OpenGL ES 程序
        mProgram = GLES30.glCreateProgram()
        // 将顶点着色器添加到程序中
        GLES30.glAttachShader(mProgram, vertexShader)
        // 将片段着色器添加到程序中
        GLES30.glAttachShader(mProgram, fragmentShader)
        // 编译链接OpenGL ES程序
        GLES30.glLinkProgram(mProgram)
        GLES30.glGetProgramiv(mProgram, GLES30.GL_COMPILE_STATUS, success)
        if (success.get(0) == 0) {

            Log.e(TAG, GLES30.glGetShaderInfoLog(mProgram))
            GLES30.glDeleteShader(mProgram)
        }
        GLES30.glDeleteShader(vertexShader);
        GLES30.glDeleteShader(fragmentShader);

        GLES30.glBindVertexArray(VAOids.get(0))

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, VBOids.get(0))
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,
                vertices3D.size * 4,
                FloatBuffer.wrap(vertices3D)
                , GLES30.GL_STATIC_DRAW)


        GLES30.glVertexAttribPointer(0,
                3,
                GLES30.GL_FLOAT,
                false,
                vertexStride,
                0)

        GLES30.glEnable(GLES30.GL_DEPTH_TEST)


    }

    fun draw() {
        //GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)


        GLES30.glEnableVertexAttribArray(0)

        // 将程序添加到OpenGL ES环境
        GLES30.glUseProgram(mProgram)

        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.scaleM(mModelMatrix, 0, 0.02f, 0.02f, 0.02f)
//        Matrix.translateM(mModelMatrix, 0,
//                mLightPosition[0],
//                mLightPosition[1],
//                mLightPosition[2])


        Matrix.setIdentityM(mViewMatrix, 0)
        Matrix.translateM(mViewMatrix, 0,
                mLightPosition[0],
                mLightPosition[1],
                mLightPosition[2])

        val displayMetrics = mContext.resources.displayMetrics
        Matrix.setIdentityM(mProjectionMatrix, 0)
        Matrix.perspectiveM(mProjectionMatrix,
                0,
                45f,
                displayMetrics.widthPixels * 1.0f / displayMetrics.heightPixels,
                0.1f,
                100f)
        val modelLoc = GLES30.glGetUniformLocation(mProgram, "model")
        GLES30.glUniformMatrix4fv(modelLoc, 1, false, mModelMatrix, 0)
        val viewLoc = GLES30.glGetUniformLocation(mProgram, "view")
        GLES30.glUniformMatrix4fv(viewLoc, 1, false, mViewMatrix, 0)
        val projectionLoc = GLES30.glGetUniformLocation(mProgram, "projection")
        GLES30.glUniformMatrix4fv(projectionLoc, 1, false, mProjectionMatrix, 0)


//        val vertexColorLocation = GLES30.glGetUniformLocation(mProgram, "outColor")
        GLES30.glBindVertexArray(VAOids.get(0))

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36)


        // 禁用顶点
        GLES30.glDisableVertexAttribArray(0)

    }

    companion object {

        // 此数组中每个顶点的维度
        internal val COORDS_PER_VERTEX = 3
        internal val vertexStride = COORDS_PER_VERTEX * 4

        var vertices3D = floatArrayOf(
                // ---- 位置 ----
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                -0.5f, 0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,

                -0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,
                -0.5f, -0.5f, 0.5f,

                -0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,

                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,

                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, -0.5f,

                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f)

    }
}