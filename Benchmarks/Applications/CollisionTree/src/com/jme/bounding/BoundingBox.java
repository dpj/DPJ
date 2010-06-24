package com.jme.bounding;
import com.jme.math.*;
import com.jme.scene.*;

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
 * <code>BoundingBox</code> defines an axis-aligned cube that defines a
 * container for a group of vertices of a particular piece of geometry. This box
 * defines a center and extents from that center along the x, y and z axis. <br>
 * <br>
 * A typical usage is to allow the class define the center and radius by calling
 * either <code>containAABB</code> or <code>averagePoints</code>. A call to
 * <code>computeFramePoint</code> in turn calls <code>containAABB</code>.
 * 
 * @author Joshua Slack
 * @version $Id: BoundingBox.java,v 1.50 2007/09/22 16:46:35 irrisor Exp $
 */
public class BoundingBox<region R> extends BoundingVolume<R> {
    public float xExtent in R, yExtent in R, zExtent in R;
    
    /**
     * Default constructor instantiates a new <code>BoundingBox</code>
     * object.
     */
    public BoundingBox() {
    }

    /**
     * Contstructor instantiates a new <code>BoundingBox</code> object with
     * given specs.
     */
    public <region Rc> BoundingBox(Vector3f<Rc> c, float x, float y, float z) reads Rc writes R {
        this.center.set(c);
        this.xExtent = x;
        this.yExtent = y;
        this.zExtent = z;
    }

    
    /**
     * Region-aware copy constructor
     */
    public BoundingBox(BoundingBox<*> bb) {
    	this.xExtent = bb.xExtent;
    	this.yExtent = bb.yExtent;
    	this.zExtent = bb.zExtent;
    	if (bb.center != null) {
    		this.center.x = bb.center.x;
    		this.center.y = bb.center.y;
    		this.center.z = bb.center.z;
    	}
    }
    
    public <region Rindices> void computeFromTris(int[]<Rindices> indices, TriMesh<Rindices> mesh, int start, int end) {
    	if (end - start <= 0) {
            return;
        }
    	
    	Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        Vector3f point;
    	Vector3f[] verts = new Vector3f[3];
        
        for (int i = start; i < end; i++) {
        	mesh.<region Root>getTriangle(indices[i], verts);
        	point = verts[0];
            checkMinMax(min, max, point);
            point = verts[1];
            checkMinMax(min, max, point);
            point = verts[2];
            checkMinMax(min, max, point);
        }
        
        center.set(min.addLocal(max));
        center.multLocal(0.5f);

        xExtent = max.x - center.x;
        yExtent = max.y - center.y;
        zExtent = max.z - center.z;
    }

    private void checkMinMax(Vector3f min, Vector3f max, Vector3f point) {
        if (point.x < min.x)
            min.x = point.x;
        else if (point.x > max.x)
            max.x = point.x;
        if (point.y < min.y)
            min.y = point.y;
        else if (point.y > max.y)
            max.y = point.y;
        if (point.z < min.z)
            min.z = point.z;
        else if (point.z > max.z)
            max.z = point.z;
    }
    
    /**
     * <code>transform</code> modifies the center of the box to reflect the
     * change made via a rotation, translation and scale.
     * 
     * @param rotate
     *            the rotation change.
     * @param translate
     *            the translation change.
     * @param scale
     *            the size change.
     * @param store
     *            box to store result in
     */
	public <region Rrotate, Rtranslate, Rscale, Rstore, RTemp1, RTemp2, RTemp3> BoundingBox<Rstore> 
	transform_r(Quaternion<Rrotate> rotate, Vector3f<Rtranslate> translate, Vector3f<Rscale> scale, 
			BoundingBox<Rstore> store, 
			Matrix3f<RTemp1> _compMat, Vector3f<RTemp2> _compVect1, Vector3f<RTemp3> _compVect2)
	reads R, Rrotate, Rtranslate, Rscale writes RTemp1, RTemp2, RTemp3, Rstore {

        BoundingBox<Rstore> box;
        if (store == null) {
            box = new BoundingBox<Rstore>();
        } else {
            box = store;
        }
        BoundingVolume<Rstore> boxVol = box;

        center.mult(scale, boxVol.center);
        rotate.mult(boxVol.center, boxVol.center);
        boxVol.center.addLocal(translate);

        Matrix3f<RTemp1> transMatrix = _compMat;
        transMatrix.set(rotate);
        // Make the rotation matrix all positive to get the maximum x/y/z extent
        transMatrix.m00 = Math.abs(transMatrix.m00);
        transMatrix.m01 = Math.abs(transMatrix.m01);
        transMatrix.m02 = Math.abs(transMatrix.m02);
        transMatrix.m10 = Math.abs(transMatrix.m10);
        transMatrix.m11 = Math.abs(transMatrix.m11);
        transMatrix.m12 = Math.abs(transMatrix.m12);
        transMatrix.m20 = Math.abs(transMatrix.m20);
        transMatrix.m21 = Math.abs(transMatrix.m21);
        transMatrix.m22 = Math.abs(transMatrix.m22);

        _compVect1.set(xExtent * scale.x, yExtent * scale.y, zExtent * scale.z);
        transMatrix.mult(_compVect1, _compVect2);
        // Assign the biggest rotations after scales.
        box.xExtent = Math.abs(_compVect2.x);
        box.yExtent = Math.abs(_compVect2.y);
        box.zExtent = Math.abs(_compVect2.z);

        return box;
    }
    
    
    /**
     * determines if this bounding box intersects a given bounding box. If the
     * two boxes intersect in any way, true is returned. Otherwise, false is
     * returned.
     * 
     * @see com.jme.bounding.BoundingVolume#intersectsBoundingBox(com.jme.bounding.BoundingBox)
     */
	public <region Rbb> boolean intersectsBoundingBox(BoundingBox<Rbb> bb) reads R, Rbb {
		BoundingVolume<Rbb> bbVol = bb;
        if (!Vector3f.isValidVector(center) || !Vector3f.isValidVector(bbVol.center)) return false;
    
        if (center.x + xExtent < bbVol.center.x - bb.xExtent
                || center.x - xExtent > bbVol.center.x + bb.xExtent) {
            return false;
        }
        else if (center.y + yExtent < bbVol.center.y - bb.yExtent
                || center.y - yExtent > bbVol.center.y + bb.yExtent) {
            return false;
        }
        else if (center.z + zExtent < bbVol.center.z - bb.zExtent
                || center.z - zExtent > bbVol.center.z + bb.zExtent) {
            return false;
        }
        else
            return true;
	}
}

