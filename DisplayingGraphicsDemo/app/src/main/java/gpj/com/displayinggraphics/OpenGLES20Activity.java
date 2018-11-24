package gpj.com.displayinggraphics;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class OpenGLES20Activity extends Activity {
    public static final String TAG = "OpenGLES20";

    private GLSurfaceView mGLView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 创建一个GLSurfaceView实例并将其设置为此Activity的ContentView。
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
    }
}