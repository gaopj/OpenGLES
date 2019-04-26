package gpj.com.camera

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

internal class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val mRenderer: MyGLRenderer

    private var mPreviousX: Float = 0.toFloat()
    private var mPreviousY: Float = 0.toFloat()

    public var mPreCameraPos = FloatArray(3)
    public var mPreCameraFront = FloatArray(3)
    public var mPreCameraUp = FloatArray(3)

    private var mPitch: Double = 0.toDouble()
    private var mYaw: Double = 0.toDouble()

    init {

        // 创建一个OpenGL ES 2.0 的context
        setEGLContextClientVersion(3)

        mRenderer = MyGLRenderer(this)

        // 设置渲染器（Renderer）以在GLSurfaceView上绘制
        setRenderer(mRenderer)

        // 仅在绘图数据发生更改时才渲染视图
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {

        // MotionEvent报告触摸屏和其他输入控件的输入详细信息。
        // 在这种情况下，这里只对触摸位置发生变化的事件感兴趣。
        val x = e.x
        val y = e.y

        when (e.action) {

            MotionEvent.ACTION_DOWN -> {
                mPreCameraPos[0] = mRenderer.mTriangle?.cameraPos?.get(0)!!
                mPreCameraPos[1] = mRenderer.mTriangle?.cameraPos?.get(1)!!
                mPreCameraPos[2] = mRenderer.mTriangle?.cameraPos?.get(2)!!
                mPreCameraFront[0] = mRenderer.mTriangle?.cameraFront?.get(0)!!
                mPreCameraFront[1] = mRenderer.mTriangle?.cameraFront?.get(1)!!
                mPreCameraFront[2] = mRenderer.mTriangle?.cameraFront?.get(2)!!
                mPreCameraUp[0] = mRenderer.mTriangle?.cameraUp?.get(0)!!
                mPreCameraUp[1] = mRenderer.mTriangle?.cameraUp?.get(1)!!
                mPreCameraUp[2] = mRenderer.mTriangle?.cameraUp?.get(2)!!
                mPitch  = Math.PI
                mYaw  =  Math.PI/2
            }
            MotionEvent.ACTION_MOVE -> {

                var dx = x - mPreviousX
                var dy = y - mPreviousY

                // reverse direction of rotation above the mid-line
                if (y > height / 2) {
                    //dx = dx * -1
                    mRenderer.mTriangle?.cameraPos =
                            Utils.vectorAdd(mRenderer.mTriangle?.cameraPos
                                    , Utils.vectorMul(mRenderer.mTriangle?.cameraFront, dy / 5))
                    mRenderer.mTriangle?.cameraPos =
                            Utils.vectorAdd(mRenderer.mTriangle?.cameraPos
                                    , Utils.vectorMul(Utils.vector3DCross(mRenderer.mTriangle?.cameraFront
                                    , (mRenderer.mTriangle?.cameraUp))
                                    , dx / 5))

                } else {
                     mPitch += dy/100
                     mYaw += dx/100
//                    if(mYaw > Math.PI*0.5)
//                        mYaw = Math.PI*0.5
//                    if(mYaw < -Math.PI*0.5)
//                        mYaw = -Math.PI*0.5
                    mRenderer.mTriangle?.cameraFront?.set(0, (Math.cos(mPitch) * Math.cos(mYaw)).toFloat())
                    mRenderer.mTriangle?.cameraFront?.set(1, Math.sin(mPitch).toFloat())
                    mRenderer.mTriangle?.cameraFront?.set(2, (Math.cos(mPitch) * Math.sin(mYaw)).toFloat())

                }

//                mRenderer.setAngle(
//                        mRenderer.getAngle() + (dx + dy) * TOUCH_SCALE_FACTOR)
                requestRender()
            }

            MotionEvent.ACTION_UP -> {
                mRenderer.mTriangle?.cameraPos?.set(0, mPreCameraPos[0])
                mRenderer.mTriangle?.cameraPos?.set(1, mPreCameraPos[1])
                mRenderer.mTriangle?.cameraPos?.set(2, mPreCameraPos[2])

                mRenderer.mTriangle?.cameraFront?.set(0, mPreCameraFront[0])
                mRenderer.mTriangle?.cameraFront?.set(1, mPreCameraFront[1])
                mRenderer.mTriangle?.cameraFront?.set(2, mPreCameraFront[2])

                mRenderer.mTriangle?.cameraUp?.set(0, mPreCameraUp[0])
                mRenderer.mTriangle?.cameraUp?.set(1, mPreCameraUp[1])
                mRenderer.mTriangle?.cameraUp?.set(2, mPreCameraUp[2])
                requestRender()
            }
        }

        mPreviousX = x
        mPreviousY = y
        return true
    }

}
