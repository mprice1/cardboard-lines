package com.mprice.abyss;

import java.util.HashMap;

/**
 * Created by mprice on 7/19/2014.
 */
public class SharedAssets {

    public static final float CAMERA_Z = 0.01f;
    public float[] mModel;
    public float[] mView;
    public float[] mProjection;
    public float[] mCamera;
    public float[] mHeadView;
    public float[] mModelView;
    public float[] mModelViewProjection;

    public HashMap<String, Model> models;
    public HashMap<String, Shader> shaders;
    public HashMap<String, Integer> textures;

    public SharedAssets() {
        models = new HashMap<String, Model>();
        shaders = new HashMap<String, Shader>();
        textures = new HashMap<String, Integer>();

        // Singleton Matrices.
        mModel = new float[16];
        mView = new float[16];
        mProjection = new float[16];
        mCamera = new float[16];
        mHeadView = new float[16];
        mModelView = new float[16];
        mModelViewProjection = new float[16];
    }
}
