package com.mprice.abyss.shots;

import android.opengl.GLES30;
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
        GLES30.glUseProgram(mShader.id);
        mShader.bindUniformLocations();

        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        // Build camera matrix.
        Matrix.setLookAtM(assets.mCamera, 0, 0.0f, 0.0f, assets.CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        headTransform.getHeadView(assets.mHeadView, 0);
    }

    @Override
    public void onDrawEye(EyeTransform eyeTransform) {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        mShader.bindAttributeLocations();
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_POSITION));
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_NORMAL));
        GLES30.glEnableVertexAttribArray(mShader.attributes.get(Shader.A_TEXCOORD));

        assets.mProjection = eyeTransform.getPerspective();
        Matrix.multiplyMM(assets.mView, 0, eyeTransform.getEyeView(), 0, assets.mCamera, 0);

        drawFloor();
        drawSphere();
    }

    private void drawFloor() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, assets.textures.get("rule_alpha"));
        GLES30.glUniform1i(mShader.uniforms.get(Shader.U_TEXTURE), 0);
        GLES30.glUniform2f(mShader.uniforms.get(Shader.U_TEXTURE_SCALE), 50.0f, 50.0f);
        GLES30.glUniform3f(mShader.uniforms.get(Shader.U_BG_COLOR), 1.0f, 1.0f, 1.0f);
        drawModel(mFloor, mShader);
        checkGLError("drawFloor");
    }

    private void drawSphere() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, assets.textures.get("rule_alpha"));
        GLES30.glUniform1i(mShader.uniforms.get(Shader.U_TEXTURE), 0);
        GLES30.glUniform2f(mShader.uniforms.get(Shader.U_TEXTURE_SCALE), 1.0f, 1.0f);
        GLES30.glUniform3f(mShader.uniforms.get(Shader.U_BG_COLOR), 0.9f, 0.9f, 0.2f);
        drawModelInstanced(mSphere, mShader, 50);
        checkGLError("drawSphere");
    }
}
