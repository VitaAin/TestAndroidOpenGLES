package com.vita.opengles.activity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.vita.opengles.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

/**
 * @Usage: 绘制三角形
 * @Step: 实现GLSurfaceView的Render，在Render中完成三角形的绘制，具体行为有：
 * 1. 加载顶点和片元着色器
 * 2. 确定需要绘制图形的坐标和颜色数据
 * 3. 创建program对象，连接顶点和片元着色器，链接program对象
 * 4. 设置视图窗口(viewport)
 * 5. 将坐标数据颜色数据传入OpenGL ES程序中
 * 6. 使颜色缓冲区的内容显示到屏幕上
 */
public class TriangleActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setContentView(mGLSurfaceView);
    }

    private void initView() {
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setRenderer(new TriangleRenderer());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }
}

class TriangleRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "TriangleRenderer";

    // 定义顶点(vertex)着色器命令语句
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix; \n" +
                    "attribute vec4 vPosition; \n" +
                    "void main(){              \n" +
                    " gl_Position = vPosition; \n" +
                    "}\n";
    // 片元着色器
    private final String fragmentShaderCode =
            "precision mediump float;\n" +
                    "uniform vec4 vColor;\n" +
                    "void main(){ \n" +
                    " gl_FragColor = vColor;\n" +
                    "}\n";

    // 三角形三个顶点坐标，原点在屏幕中心
    private float triangleCoords[] = {
            // x, y, z
            0.5f, 0.5f, 0.0f, // top
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f  // bottom right
    };
    private float color[] = {1.0f, 1.0f, 1.0f, 1.0f}; // 白色
    private int mProgram;
    private int mPositionHandle;
    private FloatBuffer mVertexBuffer;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.d(TAG, "onSurfaceCreated: ");
        // GLES20: OpenGL ES2.0版本，相应的，GLES30: OpenGL ES3.0
        // 将背景设为灰色
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1);
        // 申请底层空间
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        // 将坐标转换为FloatBuffer，用来传给OpenGL ES程序
        mVertexBuffer = byteBuffer.asFloatBuffer();
        mVertexBuffer.put(triangleCoords);
        mVertexBuffer.position(0);

        // 加载顶点着色器
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        // 加载片元着色器
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        Log.d(TAG, "onSurfaceCreated: shader:: " + vertexShader + ", " + fragmentShader);

        // 创建空的OpenGLES程序，program != 0 表示成功
        mProgram = GLES20.glCreateProgram();
        Log.d(TAG, "onSurfaceCreated: program:: " + mProgram);
        if (mProgram != 0) {// 创建成功
            // 将顶点着色器加入到程序
            GLES20.glAttachShader(mProgram, vertexShader);
            // 将片元着色器加入到程序
            GLES20.glAttachShader(mProgram, fragmentShader);
            // 连接到着色器程序
            GLES20.glLinkProgram(mProgram);
        }

        int[] linkStatus = new int[1];
        // 查看着色器连接情况
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
        Log.d(TAG, "onSurfaceCreated: linkStatus:: " + linkStatus[0]);
        if (linkStatus[0] != GLES20.GL_TRUE) {// 连接失败
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(mProgram));
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }
    }

    /**
     * 加载着色器
     *
     * @param type       着色器类型
     * @param shaderCode 着色器命令语句
     * @return 着色器
     */
    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        if (shader == 0) return shader;

        // 加载着色器脚本程序
        GLES20.glShaderSource(shader, shaderCode);
        // 编译着色器脚本程序
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];// 编译结果
        // 查看编译结果
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        Log.d(TAG, "loadShader: compiled:: " + compiled[0]);
        if (compiled[0] == 0) {// 编译失败
            // 释放申请的着色器
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        return shader;
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: ");
        // 设置视图窗口
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        Log.d(TAG, "onDrawFrame: ");
        // 将程序加入到OpenGL ES2.0环境
        GLES20.glUseProgram(mProgram);

        // 获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        Log.d(TAG, "onDrawFrame: vPosition:: " + mPositionHandle);
        // 启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // 准备三角形的坐标数据
        /*
        size：每个顶点对应的坐标个数，只能是2,3,4中的一个，默认值是4
        type：数组中每个顶点坐标的数据类型，可取常量:GL_BYTE, GL_SHORT,GL_FIXED,GL_FLOAT;
        stride:指定了连续顶点间的字节排列方式，如果为0，数组中的顶点就会被认为是按照紧凑方式排列的，默认值为0
        pointer:指定了数组中第一个顶点的首地址，默认值为0，对于Android，一般给一个IntBuffer就可以了
         */
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, true, 12, mVertexBuffer);

        // 和获取片元着色器的vColor成员句柄
        int colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // 设置绘制三角形的颜色
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        // 禁止顶点数组的句柄
//        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
