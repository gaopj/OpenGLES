package gpj.com.displayinggraphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Triangle {

    private FloatBuffer vertexBuffer;

    // 此数组中每个顶点的坐标数
    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = {   // 按逆时针顺序
            0.0f,  0.622008459f, 0.0f, // 上
            -0.5f, -0.311004243f, 0.0f, // 左下
            0.5f, -0.311004243f, 0.0f  // 右下
    };

    // 设置颜色的R（红）,G（绿）,B（蓝）,A（透明度） 值
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    public Triangle() {
        // 为形状坐标数组初始化顶点的字节缓冲区
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# squareCoords 数组长度 * 每个float占4字节)
                triangleCoords.length * 4);

        // 缓冲区读取顺序使用设备硬件的本地字节读取顺序
        bb.order(ByteOrder.nativeOrder());

        // 从ByteBuffer创建一个浮点缓冲区
        vertexBuffer = bb.asFloatBuffer();
        // 将坐标点加到FloatBuffer中
        vertexBuffer.put(triangleCoords);
        // 设置缓冲区开始读取位置，这边设置为从头开始读取
        vertexBuffer.position(0);
    }
}