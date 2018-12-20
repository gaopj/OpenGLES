package gpj.com.renderimage

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import android.util.Size
import java.nio.IntBuffer


object OpenGlUtils {
    val NO_TEXTURE = -1

    fun deleteTexture(textureID: Int) {
        GLES20.glDeleteTextures(1, intArrayOf(textureID), 0)
    }

    @JvmOverloads
    fun loadTexture(img: Bitmap, usedTexId: Int, recycle: Boolean = true): Int {
        val textures = intArrayOf(-1)
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
            //纹理也有坐标系，称UV坐标，或者ST坐标
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT.toFloat()) // S轴的拉伸方式为重复，决定采样值的坐标超出图片范围时的采样方式
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT.toFloat()) // T轴的拉伸方式为重复

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0)
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId)
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, img)
            textures[0] = usedTexId
        }
        if (recycle) {
            img.recycle()
        }
        return textures[0]
    }

    fun loadTexture(data: IntBuffer, size: Size, usedTexId: Int): Int {
        val textures = IntArray(1)
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, size.width, size.height,
                    0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data)
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId)
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, size.width,
                    size.height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data)
            textures[0] = usedTexId
        }
        return textures[0]
    }

    fun loadTextureAsBitmap(data: IntBuffer, size: Size, usedTexId: Int): Int {
        val bitmap = Bitmap
                .createBitmap(data.array(), size.width, size.height, Bitmap.Config.ARGB_8888)
        return loadTexture(bitmap, usedTexId)
    }

    /**
     * 加载着色器
     * @param strSource
     * @param iType
     * @return
     */
    fun loadShader(strSource: String, iType: Int): Int {
        val compiled = IntArray(1)
        val iShader = GLES20.glCreateShader(iType)
        GLES20.glShaderSource(iShader, strSource)
        GLES20.glCompileShader(iShader)
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.d("Load Shader Failed", "Compilation\n" + GLES20.glGetShaderInfoLog(iShader))
            return 0
        }
        return iShader
    }

    /**
     * 加载着色器程序
     * @param strVSource
     * @param strFSource
     * @return
     */
    fun loadProgram(strVSource: String, strFSource: String): Int {
        val iVShader: Int
        val iFShader: Int
        val iProgId: Int

        iVShader = loadShader(strVSource, GLES20.GL_VERTEX_SHADER) // 顶点着色器
        if (iVShader == 0) {
            Log.d("Load Program", "Vertex Shader Failed")
            return 0
        }
        iFShader = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER) // 片元着色器
        if (iFShader == 0) {
            Log.d("Load Program", "Fragment Shader Failed")
            return 0
        }

        iProgId = GLES20.glCreateProgram()

        GLES20.glAttachShader(iProgId, iVShader)
        GLES20.glAttachShader(iProgId, iFShader)

        GLES20.glLinkProgram(iProgId)
        // 获取program的链接情况
        val link = IntArray(1)
        GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0)
        if (link[0] <= 0) {
            Log.d("Load Program", "Linking Failed")
            return 0
        }
        GLES20.glDeleteShader(iVShader)
        GLES20.glDeleteShader(iFShader)
        return iProgId
    }

    fun rnd(min: Float, max: Float): Float {
        val fRandNum = Math.random().toFloat()
        return min + (max - min) * fRandNum
    }
}