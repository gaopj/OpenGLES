package gpj.com.displayinggraphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Square {

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    // 此数组中每个顶点的坐标数
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            -0.5f,  0.5f, 0.0f,   // 左上
            -0.5f, -0.5f, 0.0f,   // 左下
            0.5f, -0.5f, 0.0f,   // 右下
            0.5f,  0.5f, 0.0f }; // 右上

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // 顶点绘制顺序

    public Square() {
        // 为形状坐标数组初始化顶点的字节缓冲区
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# squareCoords 数组长度 * 每个float占4字节)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // 为绘制顺序数组 初始化字节缓冲区
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# drawOrder 数组长度 * 每个 short 占2字节)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
    }
}