package gpj.com.camera


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20.GL_MAX_VERTEX_ATTRIBS
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer


class Triangle(context: Context) {

    private val mContext: Context

    private val vertexShaderCode =
            "#version 300 es \n" +

                    " layout (location = 0) in vec3 aPos;" +
                    "layout (location = 1) in vec3 aColor;" +
                    "layout (location = 2) in vec2 aTexCoord;" +

                    "out vec3 ourColor;" +
                    "out vec2 TexCoord;" +

                    "uniform mat4 model;" +
                    "uniform mat4 view;" +
                    "uniform mat4 projection;" +

                    "void main() {" +
                    " gl_Position = projection * view * model * vec4(aPos, 1.0);" +
                    " ourColor = aColor;" +
                    " TexCoord = aTexCoord;" +
                    "}"


    private val fragmentShaderCode = (
            "#version 300 es \n " +
                    "#ifdef GL_ES\n" +
                    "precision highp float;\n" +
                    "#endif\n" +

                    "out vec4 FragColor; " +

                    "in vec3 ourColor; " +
                    "in vec2 TexCoord; " +

                    //"uniform sampler2D ourTexture;" +
                    "uniform sampler2D texture1;" +
                    "uniform sampler2D texture2;" +

                    "void main() {" +
                    // "  FragColor = vec4(ourColor, 1.0) ;" +
                    //"  FragColor =texture(ourTexture, TexCoord) ;" +
                    // " FragColor =  texture(ourTexture, TexCoord)*vec4(ourColor, 1.0);" +
                    " FragColor = mix(texture(texture1, TexCoord), texture(texture2, TexCoord), 0.2);\n" +
                    "}")


    private val mProgram: Int

    private val VBOids: IntBuffer
    private val VAOids: IntBuffer
    private val EBOids: IntBuffer

    private val textureIds: IntBuffer

    private val mModelMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)


    private var mAngle: Float = 0.toFloat()
    private var mRadius: Float = 0.toFloat()

    public var cameraPos = floatArrayOf(0.0f, 0.0f, 3.0f)
    public var cameraFront = floatArrayOf(0.0f, 0.0f, -1.0f)
    public var cameraUp = floatArrayOf(0.0f, 1.0f, 3.0f)

    public var cameraTarget = floatArrayOf(0.0f, 0.0f, 0f)
    public var cameraDirection = Utils.vectorSub(cameraPos, cameraTarget)

    public var up = floatArrayOf(0.0f, 1.0f, 0.0f)
    public var cameraRight = Utils.vector3DCross(up, cameraDirection)

    init {
        mContext = context.applicationContext
        var maxVertexAttribute = IntBuffer.allocate(1);
        GLES30.glGetIntegerv(GL_MAX_VERTEX_ATTRIBS, maxVertexAttribute);
        Log.d(TAG, "maxVertexAttribute:" + maxVertexAttribute.get(0))

        VBOids = IntBuffer.allocate(1);
        GLES30.glGenBuffers(1, VBOids)
        Log.d(TAG, "VBO:" + VBOids.get(0))

        VAOids = IntBuffer.allocate(1);
        GLES30.glGenBuffers(1, VAOids)

        EBOids = IntBuffer.allocate(1);
        GLES30.glGenBuffers(1, EBOids)

        textureIds = IntBuffer.allocate(2);
        GLES30.glGenBuffers(2, textureIds)

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
                vertices.size * 4,
                FloatBuffer.wrap(vertices)
                , GLES30.GL_STATIC_DRAW)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,
                vertices3D.size * 4,
                FloatBuffer.wrap(vertices3D)
                , GLES30.GL_STATIC_DRAW)


//        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, EBOids.get(0))
//        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER,
//                indices.size * 4,
//                IntBuffer.wrap(indices)
//                , GLES30.GL_STATIC_DRAW)

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds.get(0))
        // 为当前绑定的纹理对象设置环绕、过滤方式
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_MIRRORED_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inSampleSize = 16
        val bitmap: Bitmap = BitmapFactory.decodeResource(mContext.resources, R.drawable.wall, options)
        val buf = ByteBuffer.allocate(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(buf)
        buf.flip()
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB,
                bitmap.width, bitmap.height, 0, GLES30.GL_RGB, GLES30.GL_UNSIGNED_SHORT_5_6_5,
                buf)
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)


        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds.get(1))
        // 为当前绑定的纹理对象设置环绕、过滤方式
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_MIRRORED_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        val options2: BitmapFactory.Options = BitmapFactory.Options()
        options2.inPreferredConfig = Bitmap.Config.RGB_565
        options2.inSampleSize = 1
        val bitmap2: Bitmap = BitmapFactory.decodeResource(mContext.resources, R.drawable.android, options2)
        val buf2 = ByteBuffer.allocate(bitmap2.byteCount)
        bitmap2.copyPixelsToBuffer(buf2)
        buf2.flip()

        Log.d(TAG, " bitmap2.width:" + bitmap2.width + ", bitmap2.height:" + bitmap2.height)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB,
                bitmap2.width, bitmap2.height, 0, GLES30.GL_RGB, GLES30.GL_UNSIGNED_SHORT_5_6_5,
                buf2)
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)


        GLES30.glVertexAttribPointer(0,
                3,
                GLES30.GL_FLOAT,
                false,
                vertexStride,
                0)
        GLES30.glVertexAttribPointer(1,
                3,
                GLES30.GL_FLOAT,
                false,
                vertexStride,
                3 * 4)

        GLES30.glVertexAttribPointer(2,
                2,
                GLES30.GL_FLOAT,
                false,
                vertexStride, 3 * 4)


        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        val displayMetrics = mContext.resources.displayMetrics
        Matrix.setIdentityM(mProjectionMatrix, 0)
        Matrix.perspectiveM(mProjectionMatrix,
                0,
                45f,
                displayMetrics.widthPixels * 1.0f / displayMetrics.heightPixels,
                0.1f,
                100f)

    }

    fun draw() {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)


        GLES30.glEnableVertexAttribArray(0);
        GLES30.glEnableVertexAttribArray(1);
        GLES30.glEnableVertexAttribArray(2);

        // 将程序添加到OpenGL ES环境
        GLES30.glUseProgram(mProgram)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds.get(0))
        GLES30.glUniform1i(GLES30.glGetUniformLocation(mProgram, "texture1"), 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds.get(1))
        GLES30.glUniform1i(GLES30.glGetUniformLocation(mProgram, "texture2"), 1)
        mAngle = 5.0f * ((System.currentTimeMillis() / 300) % 360)
//        mRadius = 15.0f
//        var camX = Math.sin(mAngle.toDouble() / 5).toFloat() * mRadius
//        var camZ = Math.cos(mAngle.toDouble() / 5).toFloat() * mRadius
//        Matrix.setIdentityM(mViewMatrix, 0)
//        Matrix.setLookAtM(mViewMatrix, 0,
//                camX, 0f, camZ,
//                0.0f, 0.0f, 0.0f,
//                0.0f, 1.0f, 0.0f
//        )

        Matrix.setLookAtM(mViewMatrix, 0,
                cameraPos[0], cameraPos[1], cameraPos[2],
                cameraPos[0] + cameraFront[0],
                cameraPos[1] + cameraFront[1],
                cameraPos[2] + cameraFront[2],
                cameraUp[0], cameraUp[1], cameraUp[2]
        )
        for (i in 0..3) {

            Matrix.setIdentityM(mModelMatrix, 0)
            Matrix.translateM(mModelMatrix, 0,
                    cubePosition[i * 3],
                    cubePosition[i * 3 + 1],
                    cubePosition[i * 3 + 2] - 4f)


            Matrix.rotateM(mModelMatrix, 0, mAngle,
                    cubePosition[i * 3] + 0.5f,
                    cubePosition[i * 3 + 1] + 1.0f,
                    cubePosition[i * 3 + 2])

            val modelLoc = GLES30.glGetUniformLocation(mProgram, "model")
            GLES30.glUniformMatrix4fv(modelLoc, 1, false, mModelMatrix, 0)
            val viewLoc = GLES30.glGetUniformLocation(mProgram, "view")
            GLES30.glUniformMatrix4fv(viewLoc, 1, false, mViewMatrix, 0)
            val projectionLoc = GLES30.glGetUniformLocation(mProgram, "projection")
            GLES30.glUniformMatrix4fv(projectionLoc, 1, false, mProjectionMatrix, 0)


//        val vertexColorLocation = GLES30.glGetUniformLocation(mProgram, "outColor")
            GLES30.glBindVertexArray(VAOids.get(0))

            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36)
        }

        // 禁用顶点
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glDisableVertexAttribArray(2)
    }

    companion object {

        // 此数组中每个顶点的维度
        internal val COORDS_PER_VERTEX = 5
        internal val vertexStride = COORDS_PER_VERTEX * 4


        internal var indices = intArrayOf(// 按逆时针顺序
                0, 1, 3,  // 第一个三角形
                1, 2, 3 // 第二个三角形
                // 0, 1, 2
        )


        var vertices = floatArrayOf(
                //     ---- 位置 ----       - 纹理坐标 -
                //0.5f, 0.5f, 0.0f, 1f, 0f, // 右上
                // 0.5f, -0.5f, 0.0f, 1f, 1f, // 右下
                // -0.5f, -0.5f, 0.0f, 0f, 1f, // 左下
                //  -0.5f, 0.5f, 0.0f, 0f, 0f    // 左上

                0.5f, 0.5f, 0.0f, 1f, 0f, // 右上
                0.5f, -0.5f, 0.0f, 1f, 1f, // 右下
                -0.5f, 0.5f, 0.0f, 0f, 0f,    // 左上
                0.5f, -0.5f, 0.0f, 1f, 1f, // 右下
                -0.5f, -0.5f, 0.0f, 0f, 1f, // 左下
                -0.5f, 0.5f, 0.0f, 0f, 0f    // 左上


        )

        var vertices3D = floatArrayOf(
                // ---- 位置 ----     - 纹理坐标 -
                -0.5f, -0.5f, -0.5f, 0f, 0f,
                0.5f, -0.5f, -0.5f, 1f, 0f,
                0.5f, 0.5f, -0.5f, 1f, 1f,
                0.5f, 0.5f, -0.5f, 1f, 1f,
                -0.5f, 0.5f, -0.5f, 0f, 1f,
                -0.5f, -0.5f, -0.5f, 0f, 0f,

                -0.5f, -0.5f, 0.5f, 0f, 1f,
                0.5f, -0.5f, 0.5f, 1f, 1f,
                0.5f, 0.5f, 0.5f, 1f, 0f,
                0.5f, 0.5f, 0.5f, 1f, 0f,
                -0.5f, 0.5f, 0.5f, 0f, 0f,
                -0.5f, -0.5f, 0.5f, 0f, 1f,

                -0.5f, 0.5f, 0.5f, 1f, 00f,
                -0.5f, 0.5f, -0.5f, 1f, 1f,
                -0.5f, -0.5f, -0.5f, 0f, 1f,
                -0.5f, -0.5f, -0.5f, 0f, 1f,
                -0.5f, -0.5f, 0.5f, 0f, 0f,
                -0.5f, 0.5f, 0.5f, 1f, 0f,

                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f)
        var cubePosition = floatArrayOf(
                0.0f, 0.0f, 0.0f,
                0.9f, 1.3f, 0.4f,
                -0.5f, -1.2f, -1.5f,
                -1.8f, -1.0f, -2.3f)
    }
}