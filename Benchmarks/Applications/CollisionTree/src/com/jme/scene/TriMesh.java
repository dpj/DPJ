package com.jme.scene;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Externalizable;

import com.jme.math.*;

/*
 * Copyright (c) 2003-2008 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * <code>TriMesh</code> defines a geometry mesh. This mesh defines a three
 * dimensional object via a collection of points, colors, normals and textures.
 * The points are referenced via a indices array. This array instructs the
 * renderer the order in which to draw the points, creating triangles based on the mode set.
 * 
 * @author Mark Powell
 * @author Joshua Slack
 * @version $Id: TriMesh.java,v 1.69 2007/08/02 21:54:36 nca Exp $
 */
public class TriMesh<region RMesh> implements Externalizable {
	private static final long serialVersionUID = 1001L;
	
	public float[]<RMesh> vertexArray in RMesh;
	public int[]<RMesh> indexArray in RMesh;
	
	public Quaternion<RMesh> worldRotation in RMesh;
	public Vector3f<RMesh> worldScale in RMesh;
	public Vector3f<RMesh> worldTranslation in RMesh;
	
    /**
     * Empty Constructor to be used internally only.
     */
    public TriMesh() {
    }
    
    public Quaternion<RMesh> getWorldRotation() reads RMesh { return worldRotation; }
    public Vector3f<RMesh> getWorldScale() reads RMesh { return worldScale; }
    public Vector3f<RMesh> getWorldTranslation() reads RMesh { return worldTranslation; }

    /**
     * Returns the number of triangles contained in this mesh.
     */
    public int getTriangleCount() reads RMesh {
        return indexArray.length / 3;
    }
    
    /**
     * Stores in the <code>vertices</code> array the vertex values of triangle
     * <code>i</code>.
     * 
     * @param i
     * @param vertices
     */
    public <region R> void getTriangle(int i, Vector3f<R>[]<R> vertices) reads RMesh:* writes R:* {
        if (vertices == null) {
            vertices = new Vector3f<R>[3]<R>;
        }
        if (i < getTriangleCount() && i >= 0) {
            for (int x = 0; x < 3; x++) {
                if (vertices[x] == null) {
                    vertices[x] = new Vector3f<R>();
                }

                int vertexIndex = indexArray[i*3 + x];
                vertices[x].x = vertexArray[vertexIndex*3];
                vertices[x].y = vertexArray[vertexIndex*3 + 1];
                vertices[x].z = vertexArray[vertexIndex*3 + 2];

            }
        }
    }
    
    public <region Rindices> int[]<Rindices> getTriangleIndices(int[]<Rindices> indices) {
        int maxCount = getTriangleCount();
        if (indices == null || indices.length != maxCount)
            indices = new int[maxCount]<Rindices>;

        for (int i = 0, tLength = maxCount; i < tLength; i++) {
            indices[i] = i;
        }
        return indices;
    }
    
    /**
     * Used with serialization. Not to be called manually.
     * 
     * @param in
     *            ObjectInput
     * @throws IOException
     * @throws ClassNotFoundException
     * @see java.io.Externalizable
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        worldRotation = (Quaternion<RMesh>)in.readObject();
        worldScale = (Vector3f<RMesh>)in.readObject();
        worldTranslation = (Vector3f<RMesh>)in.readObject();
        vertexArray = (float[]<RMesh>)in.readObject();
        indexArray = (int[]<RMesh>)in.readObject();
    }

    /**
     * Used with serialization. Not to be called manually.
     * 
     * @param out
     *            ObjectOutput
     * @throws IOException
     * @see java.io.Externalizable
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(worldRotation);
        out.writeObject(worldScale);
        out.writeObject(worldTranslation);
        out.writeObject(vertexArray);
        out.writeObject(indexArray);
    }
}