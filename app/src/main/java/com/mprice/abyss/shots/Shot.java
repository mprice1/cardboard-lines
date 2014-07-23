package com.mprice.abyss.shots;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vrtoolkit.cardboard.EyeTransform;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.mprice.abyss.Model;
import com.mprice.abyss.Shader;
import com.mprice.abyss.SharedAssets;

/**
 * Created by mprice on 7/17/2014.
 */
public abstract class Shot {
    protected SharedAssets assets;

    public Shot(SharedAssets assets) {
        this.assets = assets;
    }

    public void init() {}

    public void onNewFrame(HeadTransform headTransform) {
    }

    public void onDrawEye(EyeTransform eyeTransform) {
    }

    // TODO: These general drawing utils should go somewhere else.

    public static void checkGLError(String func) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            Log.e("Shot", func + ": glError " + error);
            throw new RuntimeException(func + ": glError " + error);
        }
    }

    public void drawModelInstanced(Model.Instance m, Shader s, int instances) {
        Matrix.setIdentityM(assets.mModel, 0);
        m.transform.apply(assets.mModel);
        Matrix.multiplyMM(assets.mModelView, 0, assets.mView, 0, assets.mModel, 0);
        Matrix.multiplyMM(assets.mModelViewProjection, 0, assets.mProjection, 0, assets.mModelView, 0);

        GLES30.glUniformMatrix4fv(s.uniforms.get(Shader.U_MODEL), 1, false, assets.mModel, 0);
        GLES30.glUniformMatrix4fv(s.uniforms.get(Shader.U_MODEL_VIEW), 1, false, assets.mModelView, 0);
        GLES30.glUniformMatrix4fv(s.uniforms.get(Shader.U_MODEL_VIEW_PROJECTION), 1, false, assets.mModelViewProjection, 0);
        GLES30.glVertexAttribPointer(s.attributes.get(Shader.A_POSITION), 3, GLES30.GL_FLOAT, false, 0, m.model.vertexBuffer);
        GLES30.glVertexAttribPointer(s.attributes.get(Shader.A_NORMAL), 3, GLES30.GL_FLOAT, false, 0, m.model.normalBuffer);
        GLES30.glVertexAttribPointer(s.attributes.get(Shader.A_TEXCOORD), 2, GLES30.GL_FLOAT, false, 0, m.model.texcoordBuffer);
        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, m.model.vertCount, instances);
    }

    public void drawModel(Model.Instance m, Shader s) {
        Matrix.setIdentityM(assets.mModel, 0);
        m.transform.apply(assets.mModel);
        Matrix.multiplyMM(assets.mModelView, 0, assets.mView, 0, assets.mModel, 0);
        Matrix.multiplyMM(assets.mModelViewProjection, 0, assets.mProjection, 0, assets.mModelView, 0);

        GLES30.glUniformMatrix4fv(s.uniforms.get(Shader.U_MODEL), 1, false, assets.mModel, 0);
        GLES30.glUniformMatrix4fv(s.uniforms.get(Shader.U_MODEL_VIEW), 1, false, assets.mModelView, 0);
        GLES30.glUniformMatrix4fv(s.uniforms.get(Shader.U_MODEL_VIEW_PROJECTION), 1, false, assets.mModelViewProjection, 0);
        GLES30.glVertexAttribPointer(s.attributes.get(Shader.A_POSITION), 3, GLES30.GL_FLOAT, false, 0, m.model.vertexBuffer);
        GLES30.glVertexAttribPointer(s.attributes.get(Shader.A_NORMAL), 3, GLES30.GL_FLOAT, false, 0, m.model.normalBuffer);
        GLES30.glVertexAttribPointer(s.attributes.get(Shader.A_TEXCOORD), 2, GLES30.GL_FLOAT, false, 0, m.model.texcoordBuffer);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, m.model.vertCount);
    }

}
