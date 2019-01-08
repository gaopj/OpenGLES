package gpj.com.textures


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20.GL_MAX_VERTEX_ATTRIBS
import android.opengl.GLES30
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

                    "void main() {" +
                    " gl_Position = vec4(aPos, 1.0);" +
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

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, EBOids.get(0))
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER,
                indices.size * 4,
                IntBuffer.wrap(indices)
                , GLES30.GL_STATIC_DRAW)

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds.get(0))
        // 为当前绑定的纹理对象设置环绕、过滤方式
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_MIRRORED_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inSampleSize = 16
        val bitmap: Bitmap = BitmapFactory.decodeResource(mContext.resources, R.drawable.wall ,options)
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
        val bitmap2: Bitmap = BitmapFactory.decodeResource(mContext.resources, R.drawable.android ,options2)
        val buf2 = ByteBuffer.allocate(bitmap2.byteCount)
        bitmap2.copyPixelsToBuffer(buf2)
        buf2.flip()

        Log.d(TAG," bitmap2.width:"+ bitmap2.width+", bitmap2.height:"+ bitmap2.height)
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
                vertexStride, 6 * 4)




    }

    fun draw() {

        GLES30.glEnableVertexAttribArray(0);
        GLES30.glEnableVertexAttribArray(1);
        GLES30.glEnableVertexAttribArray(2);

        // 将程序添加到OpenGL ES环境
        GLES30.glUseProgram(mProgram)

       // GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds.get(0));
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds.get(0))
        GLES30.glUniform1i(GLES30.glGetUniformLocation(mProgram, "texture1"), 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds.get(1))
        GLES30.glUniform1i(GLES30.glGetUniformLocation(mProgram, "texture2"), 1)


//        val vertexColorLocation = GLES30.glGetUniformLocation(mProgram, "outColor")
        GLES30.glBindVertexArray(VAOids.get(0))

        //GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, 0);


        // 禁用顶点
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glDisableVertexAttribArray(2)
    }

    companion object {

        // 此数组中每个顶点的维度
        internal val COORDS_PER_VERTEX = 8
        internal val vertexStride = COORDS_PER_VERTEX * 4


        internal var indices = intArrayOf(// 按逆时针顺序
                0, 1, 3,  // 第一个三角形
                1, 2, 3 // 第二个三角形
                // 0, 1, 2
        )

//        internal var vertices = floatArrayOf(// 按逆时针顺序
//                // 位置              // 颜色
//                0.5f, -0.5f, 0.0f,  1.0f, 0.0f, 0.0f,   // 右下
//                -0.5f, -0.5f, 0.0f,  0.0f, 1.0f, 0.0f,   // 左下
//                0.0f,  0.5f, 0.0f,  0.0f, 0.0f, 1.0f    // 顶部
//        )

        var vertices = floatArrayOf(
                //     ---- 位置 ----       ---- 颜色 ----     - 纹理坐标 -
                0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1f, 0f, // 右上
                0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1f, 1f, // 右下
                -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0f, 1f, // 左下
                -0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 0f, 0f    // 左上
        )
    }
}