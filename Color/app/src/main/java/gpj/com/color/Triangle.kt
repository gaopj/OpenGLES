package gpj.com.color


import android.content.Context
import android.opengl.GLES20.GL_MAX_VERTEX_ATTRIBS
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import java.nio.FloatBuffer
import java.nio.IntBuffer


class Triangle(context: Context, light: Light) {

    private val mContext: Context
    private val mLight: Light

    private val vertexShaderCode =
            "#version 300 es \n" +

                    "layout (location = 0) in vec3 aPos;" +
                    "layout (location = 1) in vec3 aNormal;" +

                    "uniform mat4 model;" +
                    "uniform mat4 view;" +
                    "uniform mat4 projection;" +

                    "out vec3 FragPos; " +
                    "out vec3 Normal; " +

                    "void main() {" +
                    "   gl_Position = projection * view * model * vec4(aPos, 1.0);" +
                    "   FragPos = vec3(view * vec4(aPos, 1.0));" +
                    "   Normal = mat3(transpose(inverse(view))) * aNormal;" +

                    "}"


    private val fragmentShaderCode = (
            "#version 300 es \n " +
                    "#ifdef GL_ES\n" +
                    "precision highp float;\n" +
                    "#endif\n" +

                    "in vec3 Normal;" +
                    "in vec3 FragPos;" +

                    "out vec4 FragColor; " +

                    "uniform vec3 lightPos; " +
                    "uniform vec3 lightColor; " +
                    "uniform vec3 objectColor; " +
                    "uniform vec3 viewPos; " +

                    "void main() {" +

                    // ambient
                    "  float ambientStrength = 0.2;" +
                    "  vec3 ambient = ambientStrength * lightColor;" +
                    //"  FragColor = vec4( 1.0f, 0.5f, 0.31f,1.0f) ;" +

                    // diffuse
                    "  vec3 norm = normalize(Normal);" +
                    "  vec3 lightDir = normalize(lightPos - FragPos);" +
                    "  float diff = max(dot(norm, lightDir), 0.0);" +
                    "  vec3 diffuse = diff * lightColor;" +

                    // specular
                    "  float specularStrength = 0.5;" +
                    "  vec3 viewDir = normalize(viewPos - FragPos);" +
                    "  vec3 reflectDir = reflect(-lightDir, norm);" +
                    // pow函数第二个参数一定要带小数位，不然会崩溃
                    "  float spec = pow(max(dot(viewDir, reflectDir), 0.0f), 128.0);" +
                    "  vec3 specular = specularStrength * spec * lightColor; " +

                    "  vec3 result = ( ambient + diffuse + specular ) * objectColor;" +
                    "  FragColor = vec4(result, 1.0);" +
                    "}")


    private val mProgram: Int

    private val VBOids: IntBuffer
    private val VAOids: IntBuffer
    private val EBOids: IntBuffer

    private val mModelMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)


    private var mAngle: Float = 0.toFloat()

    init {
        mContext = context.applicationContext
        mLight = light
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
        GLES30.glVertexAttribPointer(1,
                3,
                GLES30.GL_FLOAT,
                false,
                vertexStride,
                3 * 4)

        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

    }

    fun draw() {
        // GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)


        GLES30.glEnableVertexAttribArray(0)
        GLES30.glEnableVertexAttribArray(1)

        // 将程序添加到OpenGL ES环境
        GLES30.glUseProgram(mProgram)

        Matrix.setIdentityM(mModelMatrix, 0)
        mAngle = 45f
        Matrix.rotateM(mModelMatrix, 0, mAngle,
                0f,
                1.0f,
                0f)

//        Matrix.translateM(mModelMatrix, 0,
//                -0.5f,
//                -0.1f,
//                0f)

        Matrix.setIdentityM(mViewMatrix, 0)
        Matrix.translateM(mViewMatrix, 0,
                0f,
                0f,
                -4.0f)


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

        val lightColor = GLES30.glGetUniformLocation(mProgram, "lightColor")
        GLES30.glUniform3f(lightColor, mLight.mLightColor[0], mLight.mLightColor[1], mLight.mLightColor[2])

        val objectColor = GLES30.glGetUniformLocation(mProgram, "objectColor")
        GLES30.glUniform3f(objectColor, 1.0f, 0.5f, 0.31f)
        val lightPos = GLES30.glGetUniformLocation(mProgram, "lightPos")
        GLES30.glUniform3f(lightPos, mLight.mLightPosition[0], mLight.mLightPosition[1], mLight.mLightPosition[2])
        val viewPos = GLES30.glGetUniformLocation(mProgram, "viewPos")
        GLES30.glUniform3f(viewPos, 0f, 0f, 0f)


//        val vertexColorLocation = GLES30.glGetUniformLocation(mProgram, "outColor")
        GLES30.glBindVertexArray(VAOids.get(0))

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36)


        // 禁用顶点
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)

    }

    companion object {

        // 此数组中每个顶点的维度
        internal val COORDS_PER_VERTEX = 6
        internal val vertexStride = COORDS_PER_VERTEX * 4

//        var vertices3D = floatArrayOf(
//                // ---- 位置 ----
//                -0.5f, -0.5f, -0.5f,
//                0.5f, -0.5f, -0.5f,
//                0.5f, 0.5f, -0.5f,
//                0.5f, 0.5f, -0.5f,
//                -0.5f, 0.5f, -0.5f,
//                -0.5f, -0.5f, -0.5f,
//
//                -0.5f, -0.5f, 0.5f,
//                0.5f, -0.5f, 0.5f,
//                0.5f, 0.5f, 0.5f,
//                0.5f, 0.5f, 0.5f,
//                -0.5f, 0.5f, 0.5f,
//                -0.5f, -0.5f, 0.5f,
//
//                -0.5f, 0.5f, 0.5f,
//                -0.5f, 0.5f, -0.5f,
//                -0.5f, -0.5f, -0.5f,
//                -0.5f, -0.5f, -0.5f,
//                -0.5f, -0.5f, 0.5f,
//                -0.5f, 0.5f, 0.5f,
//
//                0.5f, 0.5f, 0.5f,
//                0.5f, 0.5f, -0.5f,
//                0.5f, -0.5f, -0.5f,
//                0.5f, -0.5f, -0.5f,
//                0.5f, -0.5f, 0.5f,
//                0.5f, 0.5f, 0.5f,
//
//                -0.5f, -0.5f, -0.5f,
//                0.5f, -0.5f, -0.5f,
//                0.5f, -0.5f, 0.5f,
//                0.5f, -0.5f, 0.5f,
//                -0.5f, -0.5f, 0.5f,
//                -0.5f, -0.5f, -0.5f,
//
//                -0.5f, 0.5f, -0.5f,
//                0.5f, 0.5f, -0.5f,
//                0.5f, 0.5f, 0.5f,
//                0.5f, 0.5f, 0.5f,
//                -0.5f, 0.5f, 0.5f,
//                -0.5f, 0.5f, -0.5f)

        var vertices3D = floatArrayOf(
                // ---- 位置 ----    ---- 顶点法向量 ----
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
                0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
                0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
                0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
                -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,

                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,

                -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f,
                -0.5f, 0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
                -0.5f, -0.5f, 0.5f, -1.0f, 0.0f, 0.0f,
                -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f,

                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f,

                -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
                0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,

                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
                -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f)

    }
}