package gpj.com.coordinatesystems

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

const val TAG = "OpenGLES30"

class MainActivity : AppCompatActivity() {



    private var mGLView: GLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 创建一个GLSurfaceView实例并将其设置为此Activity的ContentView。
        mGLView = MyGLSurfaceView(this)
        setContentView(mGLView)
    }
}
