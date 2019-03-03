package gpj.com.transformations

import android.content.Context
import android.opengl.GLSurfaceView

internal class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val mRenderer: MyGLRenderer

    init {

        // 创建一个OpenGL ES 2.0 的context
        setEGLContextClientVersion(3)

        mRenderer = MyGLRenderer(this)

        // 设置渲染器（Renderer）以在GLSurfaceView上绘制
        setRenderer(mRenderer)

        // 仅在绘图数据发生更改时才渲染视图
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }


}
