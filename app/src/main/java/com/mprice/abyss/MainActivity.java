package com.mprice.abyss;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
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
import com.mprice.abyss.shots.RawMatShot;
import com.mprice.abyss.shots.ScratchShot;
import com.mprice.abyss.shots.Shot;

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

    private SharedAssets sharedAssets;

    private Shader mShader;

    private Shot shot;

    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader
     * @param type The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return
     */
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, code);
        GLES30.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
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
    public static void checkGLError(String func) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
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

        sharedAssets = new SharedAssets();

        // shot = new ScratchShot(sharedAssets);
        shot = new RawMatShot(sharedAssets);
    }

    public static FloatBuffer makeBufferData(float[] data) {
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
      shot.onNewFrame(headTransform);
      checkGLError("onNewFrame");
    }

    @Override
    public void onDrawEye(EyeTransform eyeTransform) {
      shot.onDrawEye(eyeTransform);
      checkGLError("onDrawEye");
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
        sharedAssets.models.put(name, m);
    }

    private void loadTexture(String name, int resId, int wrap, int minFilter, int MagFilter) {
        int texture[] = new int[1];
        GLES30.glGenTextures(1, texture, 0);
        sharedAssets.textures.put(name, texture[0]);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0]);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, wrap);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), resId);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bmp, 0);
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        bmp.recycle();
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        Log.i(TAG, "onSurfaceCreated");
        GLES30.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);

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
        sharedAssets.models.put("floor", floorModel);
        loadModel("sphere", R.raw.sphere);

        Shader shader = new Shader();
        int vertexShader = loadGLShader(GLES30.GL_VERTEX_SHADER, R.raw.basic_vertex);
        int fragmentShader = loadGLShader(GLES30.GL_FRAGMENT_SHADER, R.raw.basic_fragment);
        shader.id = GLES30.glCreateProgram();
        GLES30.glAttachShader(shader.id, vertexShader);
        GLES30.glAttachShader(shader.id, fragmentShader);
        GLES30.glLinkProgram(shader.id);
        shader.hasUniform(Shader.U_MODEL)
                .hasUniform(Shader.U_MODEL_VIEW)
                .hasUniform(Shader.U_MODEL_VIEW_PROJECTION)
                .hasUniform(Shader.U_TEXTURE)
                .hasUniform(Shader.U_TEXTURE_SCALE)
                .hasUniform(Shader.U_BG_COLOR);
        shader.hasAttribute(Shader.A_TEXCOORD)
                .hasAttribute(Shader.A_POSITION)
                .hasAttribute(Shader.A_NORMAL);
        sharedAssets.shaders.put("rule", shader);

        Shader shader2 = new Shader();
        vertexShader = loadGLShader(GLES30.GL_VERTEX_SHADER, R.raw.instanced_mv_vertex);
        fragmentShader = loadGLShader(GLES30.GL_FRAGMENT_SHADER, R.raw.instanced_mv_fragment);
        shader2.id = GLES30.glCreateProgram();
        GLES30.glAttachShader(shader2.id, vertexShader);
        GLES30.glAttachShader(shader2.id, fragmentShader);
        GLES30.glLinkProgram(shader2.id);
        shader2.hasUniform(Shader.U_PROJECTION)
                .hasUniform(Shader.U_TEXTURE)
                .hasUniform(Shader.U_TEXTURE_SCALE);

        // THESE ARENT REAL
        shader2.hasUniform(Shader.U_MODEL_VIEW);

        shader2.hasAttribute(Shader.A_TEXCOORD)
                .hasAttribute(Shader.A_POSITION)
                .hasAttribute(Shader.A_NORMAL)
                .hasAttribute(Shader.A_INSTANCE_ID)
                /*.hasAttribute(Shader.A_MODEL_VIEW_0)
                .hasAttribute(Shader.A_MODEL_VIEW_1)
                .hasAttribute(Shader.A_MODEL_VIEW_2)
                .hasAttribute(Shader.A_MODEL_VIEW_3)*/;
        sharedAssets.shaders.put("instanced_mv", shader2);

        loadTexture("rule_alpha", R.raw.rule_alpha,
                GLES30.GL_REPEAT, GLES30.GL_LINEAR_MIPMAP_LINEAR, GLES30.GL_LINEAR);
        loadTexture("rawmat", R.drawable.sqpatch_clean,
                GLES30.GL_REPEAT, GLES30.GL_LINEAR_MIPMAP_LINEAR, GLES30.GL_LINEAR);

        shot.init();

        checkGLError("onSurfaceCreated");
    }

    @Override
    public void onRendererShutdown() {
      Log.i(TAG, "onRendererShutdown");
    }
}
