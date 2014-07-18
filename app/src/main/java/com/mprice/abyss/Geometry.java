package com.mprice.abyss;

import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mprice on 7/16/2014.
 */
public class Geometry {

    public static class ObjGeometryData {
        public float[] verts;
        public float[] normals;
        public float[] texcoords;
        public ObjGeometryData(float[] v, float[] n, float[] tc) {
            this.verts = v;
            this.normals = n;
            this.texcoords = tc;
        }
    }

    public static class Vec3<T> {
        public T x, y, z;
        public Vec3 (T x, T y, T z) {
            this.x = x; this.y = y; this.z = z;
        }
        public void addIn(List<T> l) {
            l.add(this.x);
            l.add(this.y);
            l.add(this.z);
        }
    }

    public static class Vec2<T> {
        public T x, y;
        public Vec2 (T x, T y) {
            this.x = x; this.y = y;
        }
        public void addIn(List<T> l) {
            l.add(this.x);
            l.add(this.y);
        }
    }

    public static ObjGeometryData loadObj(Resources res, int id) {
        ArrayList<Vec3<Float>> objVerts = new ArrayList<Vec3<Float>>();
        ArrayList<Vec3<Float>> objNormals = new ArrayList<Vec3<Float>>();
        ArrayList<Vec2<Float>> objTcs = new ArrayList<Vec2<Float>>();
        ArrayList<Vec3<Integer>> objFaces = new ArrayList<Vec3<Integer>>();

        InputStream inputStream = res.openRawResource(id);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            String[] splitLine;
            while ((line = reader.readLine()) != null) {
                splitLine = line.split(" ");
                if (line.startsWith("v ")) {
                    objVerts.add(new Vec3<Float>(
                            Float.parseFloat(splitLine[1]),
                            Float.parseFloat(splitLine[2]),
                            Float.parseFloat(splitLine[3])));
                } else if (line.startsWith("vt ")) {
                    objTcs.add(new Vec2<Float>(
                            Float.parseFloat(splitLine[1]),
                            Float.parseFloat(splitLine[2])));
                } else if (line.startsWith("vn ")) {
                    objNormals.add(new Vec3<Float>(
                            Float.parseFloat(splitLine[1]),
                            Float.parseFloat(splitLine[2]),
                            Float.parseFloat(splitLine[3])));
                } else if (line.startsWith("f ")) {

                    int tris = splitLine.length - 3;
                    String[] fv;
                    for (int tri = 0; tri < tris; tri++) {
                        fv = splitLine[1].split("/");
                        objFaces.add(new Vec3<Integer>(
                                Integer.parseInt(fv[0]) - 1,
                                Integer.parseInt(fv[1]) - 1,
                                Integer.parseInt(fv[2]) - 1));
                        fv = splitLine[tri + 2].split("/");
                        objFaces.add(new Vec3<Integer>(
                                Integer.parseInt(fv[0]) - 1,
                                Integer.parseInt(fv[1]) - 1,
                                Integer.parseInt(fv[2]) - 1));
                        fv = splitLine[tri + 3].split("/");
                        objFaces.add(new Vec3<Integer>(
                                Integer.parseInt(fv[0]) - 1,
                                Integer.parseInt(fv[1]) - 1,
                                Integer.parseInt(fv[2]) - 1));
                    }

                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Log.d("loadObj", "Verts: " + objVerts.size());
        Log.d("loadObj", "Norms: " + objNormals.size());
        Log.d("loadObj", "Tcs: " + objTcs.size());
        Log.d("loadObj", "Faces: " + objFaces.size());

        ArrayList<Float> outVerts = new ArrayList<Float>();
        ArrayList<Float> outNormals = new ArrayList<Float>();
        ArrayList<Float> outTcs = new ArrayList<Float>();

        for (Vec3<Integer> face : objFaces) {
            objVerts.get(face.x).addIn(outVerts);
            objTcs.get(face.y).addIn(outTcs);
            objNormals.get(face.z).addIn(outNormals);
        }

        float[] v = new float[outVerts.size()];
        float[] n = new float[outNormals.size()];
        float[] tc = new float[outTcs.size()];
        for (int i = 0; i < v.length; i++) {
            v[i] = outVerts.get(i);
            n[i] = outNormals.get(i);
            if (i < tc.length) { tc[i] = outTcs.get(i); }
        }

        return new ObjGeometryData(v, n, tc);
    }

}
