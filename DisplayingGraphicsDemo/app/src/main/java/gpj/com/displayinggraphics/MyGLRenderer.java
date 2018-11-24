package gpj.com.displayinggraphics;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static gpj.com.displayinggraphics.OpenGLES20Activity.TAG;


public class MyGLRenderer implements GLSurfaceView.Renderer {

    @Override
    public void onDrawFrame(GL10 unused) {
        Log.d(TAG,"onDrawFrame");

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG,"onSurfaceCreated:"+config);

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        Log.d(TAG,"onSurfaceChanged:width = "+width+",height = "+height);

        GLES20.glViewport(0, 0, width, height);

    }
}