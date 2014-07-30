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
    // Apparently attributes have to be vectors... so a 4x4 matrix has to be passed as 4 attributes.
    public static final String A_INSTANCE_ID = "a_ID";  // Debugging...
    public static final String A_MODEL_VIEW_0 = "a_MV0";
    public static final String A_MODEL_VIEW_1 = "a_MV1";
    public static final String A_MODEL_VIEW_2 = "a_MV2";
    public static final String A_MODEL_VIEW_3 = "a_MV3";


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
    public boolean getHasUniform(String name) {
        return uniforms.containsKey(name);
    }

    public Shader hasAttribute(String name) {
        if (!attributes.containsKey(name)) {
            attributes.put(name, PARAM_UNSET);
        }
        return this;
    }

    public boolean getHasAttribute(String name) {
        return attributes.containsKey(name);
    }
}
