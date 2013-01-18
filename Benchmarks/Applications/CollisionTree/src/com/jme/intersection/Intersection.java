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

package com.jme.intersection;

import com.jme.math.*;

import DPJRuntime.*;

/**
 * <code>Intersection</code> provides functional methods for calculating the
 * intersection of some objects. All the methods are static to allow for quick
 * and easy calls. <code>Intersection</code> relays requests to specific
 * classes to handle the actual work. By providing checks to just
 * <code>BoundingVolume</code> the client application need not worry about
 * what type of bounding volume is being used.
 * 
 * @author Mark Powell
 * @version $Id: Intersection.java,v 1.26 2006/06/21 20:33:02 nca Exp $
 */
public class Intersection {

	/**
	 * EPSILON represents the error buffer used to denote a hit.
	 */
	public static final double EPSILON = 1e-12;

	/**
	 * This method tests for the intersection between two triangles defined by
	 * their vertexes. Converted to java from C code found at
	 * http://www.acm.org/jgt/papers/Moller97/tritri.html
	 * 
	 * @param v0
	 *            First triangle's first vertex.
	 * @param v1
	 *            First triangle's second vertex.
	 * @param v2
	 *            First triangle's third vertex.
	 * @param u0
	 *            Second triangle's first vertex.
	 * @param u1
	 *            Second triangle's second vertex.
	 * @param u2
	 *            Second triangle's third vertex.
	 * @return True if the two triangles intersect, false otherwise.
	 */
	/*
	 * Thread-safe versions of intersection routine
	 */
	public static <region RVals> boolean intersection_r(Vector3f<RVals> v0, Vector3f<RVals> v1, Vector3f<RVals> v2,
			Vector3f<RVals> u0, Vector3f<RVals> u1, Vector3f<RVals> u2) {
		region RTemps;
		Vector3f<RTemps> e1 = new Vector3f<RTemps>();
		Vector3f<RTemps> e2 = new Vector3f<RTemps>();
		Vector3f<RTemps> n1 = new Vector3f<RTemps>();
		Vector3f<RTemps> n2 = new Vector3f<RTemps>();	
		ArrayFloat<RTemps> isect1 = new ArrayFloat<RTemps>(2);
		ArrayFloat<RTemps> isect2 = new ArrayFloat<RTemps>(2);
		
		return Intersection.intersection_r(v1, v1, v2, u0, u1, u2, e1, e2, n1, n2, isect1, isect2);
	}
	
	public static <region RVals, RTemps> 
	    boolean intersection_r(Vector3f<RVals> v0, Vector3f<RVals> v1, 
				   Vector3f<RVals> v2, Vector3f<RVals> u0, 
				   Vector3f<RVals> u1, Vector3f<RVals> u2, 
				   Vector3f<RTemps> e1, Vector3f<RTemps> e2,
				   Vector3f<RTemps> n1, Vector3f<RTemps> n2, 
				   ArrayFloat<RTemps> isect1, ArrayFloat<RTemps> isect2) 
	writes RVals, RTemps:* {

		float d1, d2;
		float du0, du1, du2, dv0, dv1, dv2;
		Vector3f<RTemps> d = new Vector3f<RTemps>();

		float du0du1, du0du2, dv0dv1, dv0dv2;
		short index;
		float vp0, vp1, vp2;
		float up0, up1, up2;
		float bb, cc, max;
		float xx, yy, xxyy, tmp;

		/* compute plane equation of triangle(v0,v1,v2) */
		v1.subtract(v0, e1);
		v2.subtract(v0, e2);
		e1.cross(e2, n1);
		d1 = -n1.dot(v0);
		/* plane equation 1: n1.X+d1=0 */

		/*
		 * put u0,u1,u2 into plane equation 1 to compute signed distances to the
		 * plane
		 */
		du0 = n1.dot(u0) + d1;
		du1 = n1.dot(u1) + d1;
		du2 = n1.dot(u2) + d1;

		/* coplanarity robustness check */
		if (Math.abs(du0) < EPSILON)
			du0 = 0.0f;
		if (Math.abs(du1) < EPSILON)
			du1 = 0.0f;
		if (Math.abs(du2) < EPSILON)
			du2 = 0.0f;
		du0du1 = du0 * du1;
		du0du2 = du0 * du2;

		if (du0du1 > 0.0f && du0du2 > 0.0f) {
			return false;
		}

		/* compute plane of triangle (u0,u1,u2) */
		u1.subtract(u0, e1);
		u2.subtract(u0, e2);
		e1.cross(e2, n2);
		d2 = -n2.dot(u0);
		/* plane equation 2: n2.X+d2=0 */

		/* put v0,v1,v2 into plane equation 2 */
		dv0 = n2.dot(v0) + d2;
		dv1 = n2.dot(v1) + d2;
		dv2 = n2.dot(v2) + d2;

		if (Math.abs(dv0) < EPSILON)
			dv0 = 0.0f;
		if (Math.abs(dv1) < EPSILON)
			dv1 = 0.0f;
		if (Math.abs(dv2) < EPSILON)
			dv2 = 0.0f;

		dv0dv1 = dv0 * dv1;
		dv0dv2 = dv0 * dv2;

		if (dv0dv1 > 0.0f && dv0dv2 > 0.0f) { /*
											   * same sign on all of them + not
											   * equal 0 ?
											   */
			return false; /* no intersection occurs */
		}

		/* compute direction of intersection line */
		n1.cross(n2, d);

		/* compute and index to the largest component of d */
		max = Math.abs(d.x);
		index = 0;
		bb = Math.abs(d.y);
		cc = Math.abs(d.z);
		if (bb > max) {
			max = bb;
			index = 1;
		}
		if (cc > max) {
			max = cc;
			vp0 = v0.z;
			vp1 = v1.z;
			vp2 = v2.z;

			up0 = u0.z;
			up1 = u1.z;
			up2 = u2.z;

		} else if (index == 1) {
			vp0 = v0.y;
			vp1 = v1.y;
			vp2 = v2.y;

			up0 = u0.y;
			up1 = u1.y;
			up2 = u2.y;
		} else {
			vp0 = v0.x;
			vp1 = v1.x;
			vp2 = v2.x;

			up0 = u0.x;
			up1 = u1.x;
			up2 = u2.x;
		}

		/* compute interval for triangle 1 */
		Vector3f<RTemps> abc = e1;
		Vector2f<RTemps> x0x1 = new Vector2f<RTemps>();
		if (Intersection.newComputeIntervals(vp0, vp1, vp2, dv0, dv1, dv2, dv0dv1, dv0dv2,
				abc, x0x1)) {
			return Intersection.coplanarTriTri(n1, v0, v1, v2, u0, u1, u2);
		}

		/* compute interval for triangle 2 */
		Vector3f<RTemps> def = e2;
		Vector2f<RTemps> y0y1 = new Vector2f<RTemps>();
		if (Intersection.newComputeIntervals(up0, up1, up2, du0, du1, du2, du0du1, du0du2,
				def, y0y1)) {
			return Intersection.coplanarTriTri(n1, v0, v1, v2, u0, u1, u2);
		}

		xx = x0x1.x * x0x1.y;
		yy = y0y1.x * y0y1.y;
		xxyy = xx * yy;

		tmp = abc.x * xxyy;
		isect1[0] = tmp + abc.y * x0x1.y * yy;
		isect1[1] = tmp + abc.z * x0x1.x * yy;

		tmp = def.x * xxyy;
		isect2[0] = tmp + def.y * xx * y0y1.y;
		isect2[1] = tmp + def.z * xx * y0y1.x;

		Intersection.sort(isect1);
		Intersection.sort(isect2);

		if (isect1[1] < isect2[0] || isect2[1] < isect1[0]) {
			return false;
		} 
        
		return true;		
	}
	
	private static <region RTemps> void sort(ArrayFloat<RTemps> f) writes RTemps:* {
		if (f[0] > f[1]) {
			float c = f[0];
			f[0] = f[1];
			f[1] = c;
		}
	}

	private static <region Rabc, Rx> boolean newComputeIntervals(float vv0, float vv1, float vv2,
			float d0, float d1, float d2, float d0d1, float d0d2, Vector3f<Rabc> abc,
			Vector2f<Rx> x0x1) writes Rabc, Rx {
		if (d0d1 > 0.0f) {
			/* here we know that d0d2 <=0.0 */
			/*
			 * that is d0, d1 are on the same side, d2 on the other or on the
			 * plane
			 */
			abc.x = vv2;
			abc.y = (vv0 - vv2) * d2;
			abc.z = (vv1 - vv2) * d2;
			x0x1.x = d2 - d0;
			x0x1.y = d2 - d1;
		} else if (d0d2 > 0.0f) {
			/* here we know that d0d1 <=0.0 */
			abc.x = vv1;
			abc.y = (vv0 - vv1) * d1;
			abc.z = (vv2 - vv1) * d1;
			x0x1.x = d1 - d0;
			x0x1.y = d1 - d2;
		} else if (d1 * d2 > 0.0f || d0 != 0.0f) {
			/* here we know that d0d1 <=0.0 or that d0!=0.0 */
			abc.x = vv0;
			abc.y = (vv1 - vv0) * d0;
			abc.z = (vv2 - vv0) * d0;
			x0x1.x = d0 - d1;
			x0x1.y = d0 - d2;
		} else if (d1 != 0.0f) {
			abc.x = vv1;
			abc.y = (vv0 - vv1) * d1;
			abc.z = (vv2 - vv1) * d1;
			x0x1.x = d1 - d0;
			x0x1.y = d1 - d2;
		} else if (d2 != 0.0f) {
			abc.x = vv2;
			abc.y = (vv0 - vv2) * d2;
			abc.z = (vv1 - vv2) * d2;
			x0x1.x = d2 - d0;
			x0x1.y = d2 - d1;
		} else {
			/* triangles are coplanar */
			return true;
		}
		return false;
	}

	private static <region Rn, Rv> boolean coplanarTriTri(Vector3f<Rn> n, Vector3f<Rv> v0, Vector3f<Rv> v1,
			Vector3f<Rv> v2, Vector3f<Rv> u0, Vector3f<Rv> u1, Vector3f<Rv> u2) reads Rv writes Rn:* {
		Vector3f<Rn> a = new Vector3f<Rn>();
		short i0, i1;
		a.x = Math.abs(n.x);
		a.y = Math.abs(n.y);
		a.z = Math.abs(n.z);

		if (a.x > a.y) {
			if (a.x > a.z) {
				i0 = 1; /* a[0] is greatest */
				i1 = 2;
			} else {
				i0 = 0; /* a[2] is greatest */
				i1 = 1;
			}
		} else /* a[0] <=a[1] */{
			if (a.z > a.y) {
				i0 = 0; /* a[2] is greatest */
				i1 = 1;
			} else {
				i0 = 0; /* a[1] is greatest */
				i1 = 2;
			}
		}

		/* test all edges of triangle 1 against the edges of triangle 2 */
		ArrayFloat<Rn> v0f = new ArrayFloat<Rn>(3);
		v0.toArray(v0f);
		ArrayFloat<Rn> v1f = new ArrayFloat<Rn>(3);
		v1.toArray(v1f);
		ArrayFloat<Rn> v2f = new ArrayFloat<Rn>(3);
		v2.toArray(v2f);
		ArrayFloat<Rn> u0f = new ArrayFloat<Rn>(3);
		u0.toArray(u0f);
		ArrayFloat<Rn> u1f = new ArrayFloat<Rn>(3);
		u1.toArray(u1f);
		ArrayFloat<Rn> u2f = new ArrayFloat<Rn>(3);
		u2.toArray(u2f);
		if (Intersection.edgeAgainstTriEdges(v0f, v1f, u0f, u1f, u2f, i0, i1)) {
			return true;
		}

		if (Intersection.edgeAgainstTriEdges(v1f, v2f, u0f, u1f, u2f, i0, i1)) {
			return true;
		}

		if (Intersection.edgeAgainstTriEdges(v2f, v0f, u0f, u1f, u2f, i0, i1)) {
			return true;
		}

		/* finally, test if tri1 is totally contained in tri2 or vice versa */
		Intersection.pointInTri(v0f, u0f, u1f, u2f, i0, i1);
		Intersection.pointInTri(u0f, v0f, v1f, v2f, i0, i1);

		return false;
	}

	private static <region R> 
	    boolean pointInTri(ArrayFloat<R> V0, ArrayFloat<R> U0, 
			       ArrayFloat<R> U1, ArrayFloat<R> U2, 
			       int i0, int i1) 
	    reads R:* 
        {
		float a, b, c, d0, d1, d2;
		/* is T1 completly inside T2? */
		/* check if V0 is inside tri(U0,U1,U2) */
		a = U1[i1] - U0[i1];
		b = -(U1[i0] - U0[i0]);
		c = -a * U0[i0] - b * U0[i1];
		d0 = a * V0[i0] + b * V0[i1] + c;

		a = U2[i1] - U1[i1];
		b = -(U2[i0] - U1[i0]);
		c = -a * U1[i0] - b * U1[i1];
		d1 = a * V0[i0] + b * V0[i1] + c;

		a = U0[i1] - U2[i1];
		b = -(U0[i0] - U2[i0]);
		c = -a * U2[i0] - b * U2[i1];
		d2 = a * V0[i0] + b * V0[i1] + c;
		if (d0 * d1 > 0.0 && d0 * d2 > 0.0)
			return true;
		
		return false;
	}

	private static <region R> 
	    boolean edgeAgainstTriEdges(ArrayFloat<R> v0, ArrayFloat<R> v1,
					ArrayFloat<R> u0, ArrayFloat<R> u1, 
					ArrayFloat<R> u2, int i0, int i1) 
	    reads R:* 
        {
		float aX, aY;
		aX = v1[i0] - v0[i0];
		aY = v1[i1] - v0[i1];
		/* test edge u0,u1 against v0,v1 */
		if (Intersection.edgeEdgeTest(v0, u0, u1, i0, i1, aX, aY)) {
			return true;
		}
		/* test edge u1,u2 against v0,v1 */
		if (Intersection.edgeEdgeTest(v0, u1, u2, i0, i1, aX, aY)) {
			return true;
		}
		/* test edge u2,u1 against v0,v1 */
		if (Intersection.edgeEdgeTest(v0, u2, u0, i0, i1, aX, aY)) {
			return true;
		}
		return false;
	}

	private static <region R> 
	    boolean edgeEdgeTest(ArrayFloat<R> v0, ArrayFloat<R> u0, 
				 ArrayFloat<R> u1, int i0, int i1, 
				 float aX, float Ay) 
	    reads R:* 
        {
		float Bx = u0[i0] - u1[i0];
		float By = u0[i1] - u1[i1];
		float Cx = v0[i0] - u0[i0];
		float Cy = v0[i1] - u0[i1];
		float f = Ay * Bx - aX * By;
		float d = By * Cx - Bx * Cy;
		if ((f > 0 && d >= 0 && d <= f) || (f < 0 && d <= 0 && d >= f)) {
			float e = aX * Cy - Ay * Cx;
			if (f > 0) {
				if (e >= 0 && e <= f)
					return true;
			} else {
				if (e <= 0 && e >= f)
					return true;
			}
		}
		return false;
	}
}