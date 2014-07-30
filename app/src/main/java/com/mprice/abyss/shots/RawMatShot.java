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

    private static final int NUM_INSTANCES = 5;
    private Model.Instance mBlob;
    private Shader mShader;
    private FloatBuffer instanceMVs;
    private float[] instanceMVData;
    private FloatBuffer iids;

    public RawMatShot(SharedAssets assets) {
        super(assets);
    }

    private void buildMVData() {
        iids = MainActivity.makeBufferData(new float[]{1.f, 2.f, 3.f, 4.f, 5.f});
        /*
        for (int i = 0; i < NUM_INSTANCES; i++) {
            int mOff = i * 16;
            Matrix.setIdentityM(instanceMVData, mOff);
            Matrix.translateM(instanceMVData, mOff, i, i, i);
            // TODO: Rotation?
            Matrix.multiplyMM(instanceMVData, mOff, assets.mView, 0, instanceMVData, mOff);
        }
        instanceMVs = MainActivity.makeBufferData(instanceMVData);
        */
    }

    @Override
    public void init() {
      instanceMVData = new float[NUM_INSTANCES * 16];
      mBlob = new Model.Instance(assets.models.get("sphere"));
      mShader = assets.shaders.get("instanced_mv");
      buildMVData();
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
        checkGLError("AA");
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        mShader.bindAttributeLocations();
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_POSITION));
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_NORMAL));
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_TEXCOORD));
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_INSTANCE_ID));

        /*
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_MODEL_VIEW_0));
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_MODEL_VIEW_1));
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_MODEL_VIEW_2));
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_MODEL_VIEW_3));
        */

        assets.mProjection = eyeTransform.getPerspective();
        Matrix.multiplyMM(assets.mView, 0, eyeTransform.getEyeView(), 0, assets.mCamera, 0);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, assets.textures.get("rawmat"));
        GLES30.glUniform1i(mShader.uniforms.get(Shader.U_TEXTURE), 0);
        GLES30.glUniform2f(mShader.uniforms.get(Shader.U_TEXTURE_SCALE), 1.0f, 1.0f);

        GLES30.glUniformMatrix4fv(mShader.uniforms.get(Shader.U_PROJECTION), 1, false, assets.mProjection, 0);
        Matrix.setIdentityM(assets.mModel, 0);
        Matrix.translateM(assets.mModel, 0, 2, 2, 2);
        Matrix.multiplyMM(assets.mModelView, 0, assets.mView, 0, assets.mModel, 0);
        GLES30.glUniformMatrix4fv(mShader.uniforms.get(Shader.U_MODEL_VIEW), 1, false, assets.mModelView, 0);
        checkGLError("BB");


        /*int STRIDE = 16 * 4;
        GLES30.glVertexAttribPointer(mShader.attributes.get(Shader.A_MODEL_VIEW_0), 4, GLES30.GL_FLOAT, false, STRIDE, instanceMVs);
        instanceMVs.position(4);
        GLES30.glVertexAttribPointer(mShader.attributes.get(Shader.A_MODEL_VIEW_1), 4, GLES30.GL_FLOAT, false, STRIDE, instanceMVs);
        instanceMVs.position(8);
        GLES30.glVertexAttribPointer(mShader.attributes.get(Shader.A_MODEL_VIEW_2), 4, GLES30.GL_FLOAT, false, STRIDE, instanceMVs);
        instanceMVs.position(12);
        GLES30.glVertexAttribPointer(mShader.attributes.get(Shader.A_MODEL_VIEW_3), 4, GLES30.GL_FLOAT, false, STRIDE, instanceMVs);
        instanceMVs.position(0);*/
        GLES30.glVertexAttribPointer(mShader.attributes.get(Shader.A_INSTANCE_ID), 1, GLES30.GL_FLOAT, false, 0, iids);
        GLES30.glVertexAttribDivisor(mShader.attributes.get(Shader.A_INSTANCE_ID), 0);



        checkGLError("CCC");
        /*GLES30.glVertexAttribDivisor(mShader.attributes.get(Shader.A_MODEL_VIEW_0), 1);
        GLES30.glVertexAttribDivisor(mShader.attributes.get(Shader.A_MODEL_VIEW_1), 1);
        GLES30.glVertexAttribDivisor(mShader.attributes.get(Shader.A_MODEL_VIEW_2), 1);
        GLES30.glVertexAttribDivisor(mShader.attributes.get(Shader.A_MODEL_VIEW_3), 1);*/

        checkGLError("CCCA");
        drawModelInstanced(mBlob, mShader, NUM_INSTANCES);
        checkGLError("DD");
    }
}
