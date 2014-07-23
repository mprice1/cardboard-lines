package com.mprice.abyss.shots;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.google.vrtoolkit.cardboard.EyeTransform;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.mprice.abyss.MainActivity;
import com.mprice.abyss.Model;
import com.mprice.abyss.Shader;
import com.mprice.abyss.SharedAssets;

import java.nio.FloatBuffer;

/**
 * Created by mprice on 7/20/2014.
 */
public class RawMatShot extends Shot {

    private static final int NUM_INSTANCES = 50;
    private Model.Instance mBlob;
    private Shader mShader;
    private FloatBuffer instanceMVs;
    private float[] instanceMVData;

    public RawMatShot(SharedAssets assets) {
        super(assets);
    }

    private void buildMVData() {
        for (int i = 0; i < NUM_INSTANCES; i++) {
            int mOff = i * 16;
            Matrix.setIdentityM(instanceMVData, mOff);
            Matrix.translateM(instanceMVData, mOff, i, i, i);
        }
        instanceMVs = MainActivity.makeBufferData(instanceMVData);
    }

    @Override
    public void init() {
      instanceMVData = new float[NUM_INSTANCES];
      mBlob = new Model.Instance(assets.models.get("sphere"));
      mShader = assets.shaders.get("instanced_mv");
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        GLES30.glUseProgram(mShader.id);
        mShader.bindUniformLocations();

        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        // Build camera matrix.
        Matrix.setLookAtM(assets.mCamera, 0, 0.0f, 0.0f, assets.CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        headTransform.getHeadView(assets.mHeadView, 0);
    }

    public void onDrawEye(EyeTransform eyeTransform) {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        mShader.bindAttributeLocations();
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_POSITION));
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_NORMAL));
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_TEXCOORD));
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_MODEL_VIEW));

        assets.mProjection = eyeTransform.getPerspective();
        Matrix.multiplyMM(assets.mView, 0, eyeTransform.getEyeView(), 0, assets.mCamera, 0);

        buildMVData();

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, assets.textures.get("rawmat"));
        GLES30.glUniform1i(mShader.uniforms.get(Shader.U_TEXTURE), 0);
        GLES30.glUniform2f(mShader.uniforms.get(Shader.U_TEXTURE_SCALE), 1.0f, 1.0f);

        GLES30.glUniformMatrix4fv(mShader.uniforms.get(Shader.U_PROJECTION), 1, false, assets.mProjection, 0);

        drawModelInstanced(mBlob, mShader, 50);
    }
}
