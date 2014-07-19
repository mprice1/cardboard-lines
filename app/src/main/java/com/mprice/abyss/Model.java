package com.mprice.abyss;

import java.nio.FloatBuffer;

/**
 * Created by mprice on 7/19/2014.
 */
public class Model {
    public static class Instance {
        public Model model;
        public Transform transform;
        public Instance(Model model) {
            this.model = model;
            this.transform = new Transform();
        }
    }

    public int vertCount;
    public FloatBuffer vertexBuffer;
    public FloatBuffer normalBuffer;
    public FloatBuffer texcoordBuffer;
    public Model() {}
}