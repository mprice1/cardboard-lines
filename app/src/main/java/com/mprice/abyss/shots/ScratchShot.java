package com.mprice.abyss.shots;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.vrtoolkit.cardboard.EyeTransform;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.mprice.abyss.Model;
import com.mprice.abyss.Shader;
import com.mprice.abyss.SharedAssets;

/**
 * Created by mprice on 7/19/2014.
 */
public class ScratchShot extends Shot {

    private Model.Instance mSphere;
    private Model.Instance mFloor;
    private Shader mShader;

    public ScratchShot(SharedAssets assets) {
        super(assets);
    }

    @Override
    public void init() {
        mSphere = new Model.Instance(assets.models.get("sphere"));
        mSphere.transform.position(0, 0, 5.0f);
        mFloor = new Model.Instance(assets.models.get("floor"));
        mFloor.transform.position(0, -6.f, 0);
        mFloor.transform.scale(50.0f, 1.0f, 50.0f);
        mShader = assets.shaders.get("rule");
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        GLES20.glUseProgram(mShader.id);
        mShader.bindUniformLocations();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Build camera matrix.
        Matrix.setLookAtM(assets.mCamera, 0, 0.0f, 0.0f, assets.CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        headTransform.getHeadView(assets.mHeadView, 0);
    }

    @Override
    public void onDrawEye(EyeTransform eyeTransform) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mShader.bindAttributeLocations();
        GLES20.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_POSITION));
        GLES20.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_NORMAL));
        GLES20.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_TEXCOORD));

        assets.mProjection = eyeTransform.getPerspective();
        Matrix.multiplyMM(assets.mView, 0, eyeTransform.getEyeView(), 0, assets.mCamera, 0);

        drawFloor();
        drawSphere();
    }

    private void drawFloor() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, assets.textures.get("rule_alpha"));
        GLES20.glUniform1i(mShader.uniforms.get(Shader.U_TEXTURE), 0);
        GLES20.glUniform2f(mShader.uniforms.get(Shader.U_TEXTURE_SCALE), 50.0f, 50.0f);
        GLES20.glUniform3f(mShader.uniforms.get(Shader.U_BG_COLOR), 1.0f, 1.0f, 1.0f);
        drawModel(mFloor, mShader);
        checkGLError("drawFloor");
    }

    private void drawSphere() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, assets.textures.get("rule_alpha"));
        GLES20.glUniform1i(mShader.uniforms.get(Shader.U_TEXTURE), 0);
        GLES20.glUniform2f(mShader.uniforms.get(Shader.U_TEXTURE_SCALE), 1.0f, 1.0f);
        GLES20.glUniform3f(mShader.uniforms.get(Shader.U_BG_COLOR), 0.9f, 0.9f, 0.2f);
        drawModel(mSphere, mShader);
        checkGLError("drawSphere");
    }
}
