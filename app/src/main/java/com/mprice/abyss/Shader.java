package com.mprice.abyss;

import android.opengl.GLES30;

import java.util.HashMap;

/**
 * Created by mprice on 7/19/2014.
 */
public class Shader {
    // Standard uniform names shared between most shaders.
    // Not all shaders use all of these, but if they use the concept, they
    // should use the name.
    public static final String U_MODEL = "u_M";
    public static final String U_VIEW = "u_V";
    public static final String U_PROJECTION = "u_P";
    public static final String U_MODEL_VIEW = "u_MV";
    public static final String U_MODEL_VIEW_PROJECTION = "u_MVP";
    public static final String U_TEXTURE = "u_Texture";
    public static final String U_TEXTURE_SCALE = "u_TexScale";
    public static final String U_TEXTURE_OFFSET = "u_TexOffset";
    public static final String U_BG_COLOR = "u_BgColor";

    // Same for attributes.
    public static final String A_POSITION = "a_Position";
    public static final String A_NORMAL = "a_Normal";
    public static final String A_TEXCOORD = "a_Texcoord";
    // For instanced rendering.
    public static final String A_MODEL_VIEW = "a_MV";


    public static final int PARAM_UNSET = -1;

    public HashMap<String, Integer> uniforms;
    public HashMap<String, Integer> attributes;
    public int id;

    public Shader() {
        uniforms = new HashMap<String, Integer>();
        attributes = new HashMap<String, Integer>();
    }

    public void bindAttributeLocations() {
        for (String param : attributes.keySet()) {
            attributes.put(param, GLES30.glGetAttribLocation(id, param));
        }
        attributes.put(A_POSITION, GLES30.glGetAttribLocation(id, A_POSITION));
        attributes.put(A_NORMAL, GLES30.glGetAttribLocation(id, A_NORMAL));
        attributes.put(A_TEXCOORD, GLES30.glGetAttribLocation(id, A_TEXCOORD));
    }

    public void bindUniformLocations() {
        for (String param : uniforms.keySet()) {
            uniforms.put(param, GLES30.glGetUniformLocation(id, param));
        }
    }

    public Shader hasUniform(String name) {
        if (!uniforms.containsKey(name)) {
            uniforms.put(name, PARAM_UNSET);
        }
        return this;
    }
    public Shader hasAttribute(String name) {
        if (!attributes.containsKey(name)) {
            attributes.put(name, PARAM_UNSET);
        }
        return this;
    }
}
