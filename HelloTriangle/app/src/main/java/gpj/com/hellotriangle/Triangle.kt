package gpj.com.hellotriangle


import android.opengl.GLES30
import android.util.Log
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Triangle {

//    private val vertexShaderCode =
//            "attribute vec4 vPosition;" +
//                    "void main() {" +
//                    "  gl_Position = vPosition;" +
//                    "}"

    private val vertexShaderCode =
            "#version 300 es \n" +
                    " layout (location = 0) in vec3 aPos;" +
                    "void main() {" +
                    " gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);" +
                    "}"

    private val fragmentShaderCode = (
            "#version 300 es \n " +
                    "#ifdef GL_ES\n"+
                    "precision mediump float;\n"+
                    "#endif\n"+
                    "out vec4 FragColor; " +
                    "void main() {" +
                    "  FragColor = vec4(1.0f,0.5f,0.2f,1.0f);" +
                    "}")


    private val mProgram: Int

    private val VBO: IntBuffer
    private val VAO: IntBuffer
    private val EBO: IntBuffer


    // 设置颜色的R（红）,G（绿）,B（蓝）,A（透明度） 值
    internal var color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    private var mPositionHandle: Int = 0

    //private val vertexCount = triangleCoords.size / COORDS_PER_VERTEX
    private val vertexCount = squareCoords.size / COORDS_PER_VERTEX
    private val vertexStride = COORDS_PER_VERTEX * 4 //一个顶点占用空间，其中每个顶点单维值占4字节

    init {

        VBO = IntBuffer.allocate(1);
        GLES30.glGenBuffers(1, VBO)
        Log.d(TAG, "VBO:" + VBO.get(0))

        VAO = IntBuffer.allocate(1);
        GLES30.glGenBuffers(1, VAO)

        EBO = IntBuffer.allocate(1);
        GLES30.glGenBuffers(1, EBO)


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

        GLES30.glBindVertexArray(VAO.get(0))

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, VBO.get(0))
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,
                squareCoords.size * 4,
                FloatBuffer.wrap(squareCoords)
                , GLES30.GL_STATIC_DRAW)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, EBO.get(0))
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER,
                indices.size * 4,
                IntBuffer.wrap(indices)
                , GLES30.GL_STATIC_DRAW)

        GLES30.glVertexAttribPointer(0,
                3,
                GLES30.GL_FLOAT,
                false,
                vertexStride,
                0)



    }

    fun draw() {

        GLES30.glEnableVertexAttribArray(0);
        // 将程序添加到OpenGL ES环境
        GLES30.glUseProgram(mProgram)

        GLES30.glBindVertexArray(VAO.get(0))

        // 绘制三角形
        //GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, 0);


        // 禁用顶点
        GLES30.glDisableVertexAttribArray(0)
    }

    companion object {

        // 此数组中每个顶点的维度
        internal val COORDS_PER_VERTEX = 3
        internal val vertexStride = COORDS_PER_VERTEX * 4
//        internal var triangleCoords = floatArrayOf(// 按逆时针顺序
//                0.0f, 0.622008459f, 0.0f, // 上
//                -0.5f, -0.311004243f, 0.0f, // 左下
//                0.5f, -0.311004243f, 0.0f  // 右下
//        )

        internal var squareCoords = floatArrayOf(// 按逆时针顺序
                0.5f, 0.5f, 0.0f,   // 右上角
                0.5f, -0.5f, 0.0f,  // 右下角
                -0.5f, -0.5f, 0.0f, // 左下角
                -0.5f, 0.5f, 0.0f   // 左上角
        )

        internal var indices = intArrayOf(// 按逆时针顺序
                0, 1, 3,   // 第一个三角形
                1, 2, 3 // 第二个三角形
        )
    }
}