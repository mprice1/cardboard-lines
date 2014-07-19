package com.mprice.abyss;

import android.opengl.Matrix;

/**
 * Created by mprice on 7/19/2014.
 */
public class Transform {
    public float x, y, z,
            rx, ry, rz,
            sx, sy, sz;
    public Transform() {
        x = y = z = rx = ry = rz = 0;
        this.sx = 1.0f; this.sy = 1.0f; this.sz = 1.0f;
    }
    public Transform position(float x, float y, float z) {
        this.x = x; this.y = y; this.z = z;
        return this;
    }
    public Transform rotation(float x, float y, float z) {
        this.rx = x; this.ry = y; this.rz = z;
        return this;
    }
    public Transform scale(float x, float y, float z) {
        this.sx = x; this.sy = y; this.sz = z;
        return this;
    }

    public void apply(float[] m) {
        Matrix.translateM(m, 0, x, y, z);
        // TODO: Do rotation better.
        Matrix.rotateM(m, 0, rx, 1.0f, 0, 0);
        Matrix.rotateM(m, 0, ry, 0, 1.0f, 0);
        Matrix.rotateM(m, 0, rz, 0, 0, 1.0f);
        Matrix.scaleM(m, 0, sx, sy, sz);
    }
}
