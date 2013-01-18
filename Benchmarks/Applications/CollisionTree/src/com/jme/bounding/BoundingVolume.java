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

package com.jme.bounding;

import java.io.Serializable;

import com.jme.math.*;
import com.jme.scene.*;

import DPJRuntime.*;

/**
 * <code>BoundingVolume</code> defines an interface for dealing with
 * containment of a collection of points.
 * 
 * @author Mark Powell
 * @version $Id: BoundingVolume.java,v 1.24 2007/09/21 15:45:32 nca Exp $
 */
public abstract class BoundingVolume<region R> {
    public enum Type {
        Sphere, AABB, OBB, Capsule;
    }
    
    protected Vector3f<R> center in R = new Vector3f<R>();
    
	public BoundingVolume() pure {
    }

	/**
	 * 
	 * <code>transform</code> alters the location of the bounding volume by a
	 * rotation, translation and a scalar.
	 * 
	 * @param rotate
	 *            the rotation to affect the bound.
	 * @param translate
	 *            the translation to affect the bound.
	 * @param scale
	 *            the scale to resize the bound.
	 * @param store
	 *            sphere to store result in
	 * @return the new bounding volume.
	 */
	//public abstract BoundingVolume transform(Quaternion rotate, Vector3f translate, Vector3f scale, BoundingVolume store);
	public abstract <region Rrotate, Rtranslate, Rscale, Rstore, RTemp1, RTemp2, RTemp3> BoundingBox<Rstore>
	transform_r(Quaternion<Rrotate> rotate, Vector3f<Rtranslate> translate, Vector3f<Rscale> scale, 
			BoundingBox<Rstore> store, 
			Matrix3f<RTemp1> _compMat, Vector3f<RTemp2> _compVect1, Vector3f<RTemp3> _compVect2)
	reads R, Rrotate, Rtranslate, Rscale writes RTemp1, RTemp2, RTemp3, Rstore;
	
	/**
	 * determines if this bounding volume and a given bounding box are
	 * intersecting.
	 * 
	 * @param bb
	 *            the bounding box to test against.
	 * @return true if this volume intersects the given bounding box.
	 */
	public abstract <region Rbb> boolean intersectsBoundingBox(BoundingBox<Rbb> bb) reads R, Rbb;
	
	public abstract <region Rindices> void computeFromTris(ArrayInt<Rindices> triIndex, 
							       TriMesh<Rindices> mesh, 
							       int start, int end);
}

