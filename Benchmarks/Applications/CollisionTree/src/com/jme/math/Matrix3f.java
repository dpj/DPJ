package com.jme.math;
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

import java.io.Serializable;

/**
 * <code>Matrix3f</code> defines a 3x3 matrix. Matrix data is maintained
 * internally and is accessible via the get and set methods. Convenience methods
 * are used for matrix operations as well as generating a matrix from a given
 * set of values.
 * 
 * @author Mark Powell
 * @author Joshua Slack
 */
public class Matrix3f<region R>  implements Serializable {
    private static final long serialVersionUID = 1L;

    public float m00 in R, m01 in R, m02 in R;
    public float m10 in R, m11 in R, m12 in R;
    public float m20 in R, m21 in R, m22 in R;

    /**
     * Constructor instantiates a new <code>Matrix3f</code> object. The
     * initial values for the matrix is that of the identity matrix.
     *  
     */
    public Matrix3f() {}

    /**
     * 
     * <code>set</code> defines the values of the matrix based on a supplied
     * <code>Quaternion</code>. It should be noted that all previous values
     * will be overridden.
     * 
     * @param quaternion
     *            the quaternion to create a rotational matrix from.
     */
    public <region Rquaternion> void 
    set(Quaternion<Rquaternion> quaternion) 
    reads Rquaternion writes R {
        quaternion.toRotationMatrix(this);
    }

    /**
     * Multiplies this 3x3 matrix by the 1x3 Vector vec and stores the result in
     * product.
     * 
     * @param vec
     *            The Vector3f to multiply.
     * @param product
     *            The Vector3f to store the result, it is safe for this to be
     *            the same as vec.
     * @return The given product vector.
     */
    public <region Rvec, Rproduct> Vector3f<Rproduct>
    mult(Vector3f<Rvec> vec, Vector3f<Rproduct> product) 
    reads R, Rvec writes Rproduct {
        
        if (null == product) {
            product = new Vector3f<Rproduct>();
        }

        float x = vec.x;
        float y = vec.y;
        float z = vec.z;

        product.x = m00 * x + m01 * y + m02 * z;
        product.y = m10 * x + m11 * y + m12 * z;
        product.z = m20 * x + m21 * y + m22 * z;
        return product;
    }

}