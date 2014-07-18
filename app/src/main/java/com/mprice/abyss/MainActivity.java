package com.mprice.abyss;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.EyeTransform;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;


public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {

    private static final String TAG = "MainActivity";
    private static final float CAMERA_Z = 0.01f;

    private int mGlProgram;
    private int mModelViewProjectionParam;
    private int mModelViewParam;
    private int mModelParam;
    private int mPositionParam;
    private int mNormalParam;
    private int mTexcoordParam;
    private int mTextureParam;
    private int mTexScaleParam;
    private int mBgColorParam;

    private float[] mModel;
    private float[] mView;
    private float[] mProjection;

    private float[] mCamera;
    private float[] mHeadView;
    private float[] mModelView;
    private float[] mModelViewProjection;

    private HashMap<String, Model> models;

    private int mTexture;

    private ModelInstance mSphere;
    private ModelInstance mFloor;



    public static class Model {
        public int vertCount;
        public FloatBuffer vertexBuffer;
        public FloatBuffer normalBuffer;
        public FloatBuffer texcoordBuffer;
    }

    public static class ModelInstance {
        public Model model;
        public float x, y, z,
                rx, ry, rz,
                sx, sy, sz;
        public ModelInstance(Model model) {
            this.model = model;
            x = y = z = rx = ry = rz = 0;
            this.sx = 1.0f; this.sy = 1.0f; this.sz = 1.0f;
        }
        public ModelInstance position(float x, float y, float z) {
            this.x = x; this.y = y; this.z = z;
            return this;
        }
        public ModelInstance rotation(float x, float y, float z) {
            this.rx = x; this.ry = y; this.rz = z;
            return this;
        }
        public ModelInstance scale(float x, float y, float z) {
            this.sx = x; this.sy = y; this.sz = z;
            return this;
        }
    }

    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader
     * @param type The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return
     */
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    /**
     * Converts a raw text file into a string.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     * @param func
     */
    private static void checkGLError(String func) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, func + ": glError " + error);
            throw new RuntimeException(func + ": glError " + error);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        models = new HashMap<String, Model>();

        // Singleton Matrices.
        mCamera = new float[16];
        mView = new float[16];
        mModel = new float[16];
        mProjection = new float[16];
        mModelView = new float[16];
        mModelViewProjection = new float[16];
        mHeadView = new float[16];
    }

    private FloatBuffer makeBufferData(float[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(data);
        fb.position(0);
        return fb;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
      GLES20.glUseProgram(mGlProgram);

      // Attach constant shader params.
      mModelViewProjectionParam = GLES20.glGetUniformLocation(mGlProgram, "u_MVP");
      mModelViewParam = GLES20.glGetUniformLocation(mGlProgram, "u_MV");
      mModelParam = GLES20.glGetUniformLocation(mGlProgram, "u_M");
      mTextureParam  = GLES20.glGetUniformLocation(mGlProgram, "u_Texture");
      mTexScaleParam = GLES20.glGetUniformLocation(mGlProgram, "u_TexScale");
      mBgColorParam = GLES20.glGetUniformLocation(mGlProgram, "u_BgColor");

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

      // Build camera matrix.
      Matrix.setLookAtM(mCamera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
      headTransform.getHeadView(mHeadView, 0);

      checkGLError("onNewFrame");
    }

    @Override
    public void onDrawEye(EyeTransform eyeTransform) {

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mPositionParam = GLES20.glGetAttribLocation(mGlProgram, "a_Position");
        mNormalParam = GLES20.glGetAttribLocation(mGlProgram, "a_Normal");
        mTexcoordParam = GLES20.glGetAttribLocation(mGlProgram, "a_Texcoord");

        GLES20.glEnableVertexAttribArray(mPositionParam);
        GLES20.glEnableVertexAttribArray(mNormalParam);
        GLES20.glEnableVertexAttribArray(mTexcoordParam);

        mProjection = eyeTransform.getPerspective();
        Matrix.multiplyMM(mView, 0, eyeTransform.getEyeView(), 0, mCamera, 0);

        drawFloor();
        drawSphere();

        checkGLError("drawEye");
    }

    private void drawFloor() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture);
        GLES20.glUniform1i(mTextureParam, 0);
        GLES20.glUniform2f(mTexScaleParam, 5.0f, 5.0f);
        GLES20.glUniform3f(mBgColorParam, 1.0f, 1.0f, 1.0f);
        drawModel(mFloor);
        checkGLError("drawFloor");
    }

    private void drawSphere() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture);
        GLES20.glUniform1i(mTextureParam, 0);
        GLES20.glUniform2f(mTexScaleParam, 1.0f, 1.0f);
        GLES20.glUniform3f(mBgColorParam, 0.9f, 0.9f, 0.2f);
        drawModel(mSphere);
        checkGLError("drawFloor");
    }

    private void drawModel(ModelInstance m) {
        Matrix.setIdentityM(mModel, 0);
        Matrix.translateM(mModel, 0, m.x, m.y, m.z);
        // TODO: Do rotation better.
        Matrix.rotateM(mModel, 0, m.rx, 1.0f, 0, 0);
        Matrix.rotateM(mModel, 0, m.ry, 0, 1.0f, 0);
        Matrix.rotateM(mModel, 0, m.rz, 0, 0, 1.0f);
        Matrix.scaleM(mModel, 0, m.sx, m.sy, m.sz);
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModel, 0);
        Matrix.multiplyMM(mModelViewProjection, 0, mProjection, 0, mModelView, 0);

        GLES20.glUniformMatrix4fv(mModelParam, 1, false, mModel, 0);
        GLES20.glUniformMatrix4fv(mModelViewParam, 1, false, mModelView, 0);
        GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false, mModelViewProjection, 0);

        GLES20.glVertexAttribPointer(mPositionParam, 3, GLES20.GL_FLOAT, false, 0, m.model.vertexBuffer);
        GLES20.glVertexAttribPointer(mNormalParam, 3, GLES20.GL_FLOAT, false, 0, m.model.normalBuffer);
        GLES20.glVertexAttribPointer(mTexcoordParam, 2, GLES20.GL_FLOAT, false, 0, m.model.texcoordBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, m.model.vertCount);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i2) {
        Log.i(TAG, "onSurfaceChanged");
    }

    private void loadModel(String name, int resId) {
        Geometry.ObjGeometryData geo = Geometry.loadObj(getResources(), resId);
        Model m = new Model();
        m.normalBuffer = makeBufferData(geo.normals);
        m.vertexBuffer = makeBufferData(geo.verts);
        m.texcoordBuffer = makeBufferData(geo.texcoords);
        m.vertCount = geo.verts.length / 3;
        models.put(name, m);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);

        Model floorModel = new Model();
        floorModel.vertexBuffer = makeBufferData(new float[] {
                10f, 0, -10f,
                -10f, 0, -10f,
                -10f, 0, 10f,
                10f, 0, -10f,
                -10f, 0, 10f,
                10f, 0, 10f,
        });
        floorModel.normalBuffer = makeBufferData(new float[] {
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
        });
        floorModel.texcoordBuffer = makeBufferData(new float[] {
                1.0f, 0.0f,
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
        });
        floorModel.vertCount = 6;
        models.put("floor", floorModel);
        loadModel("sphere", R.raw.sphere);

        mSphere = new ModelInstance(models.get("sphere"));
        mSphere.position(0, 0, 5.0f);
        mFloor = new ModelInstance(models.get("floor"));
        mFloor.position(0, -6.f, 0);

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.basic_vertex);
        int fragmentShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.basic_fragment);
        mGlProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mGlProgram, vertexShader);
        GLES20.glAttachShader(mGlProgram, fragmentShader);
        GLES20.glLinkProgram(mGlProgram);

        // Load the texture.
        int texture[] = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        mTexture = texture[0];
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.raw.rule_alpha);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        bmp.recycle();

        checkGLError("onSurfaceCreated");
    }

    @Override
    public void onRendererShutdown() {
      Log.i(TAG, "onRendererShutdown");
    }
}
