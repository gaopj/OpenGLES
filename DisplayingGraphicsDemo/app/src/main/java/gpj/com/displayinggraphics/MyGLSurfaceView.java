package gpj.com.displayinggraphics;

import android.content.Context;
import android.opengl.GLSurfaceView;

class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context){
        super(context);

        // 创建一个OpenGL ES 2.0 的context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer();

        // 设置渲染器（Renderer）以在GLSurfaceView上绘制
        setRenderer(mRenderer);

        // 仅在绘图数据发生更改时才渲染视图
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
