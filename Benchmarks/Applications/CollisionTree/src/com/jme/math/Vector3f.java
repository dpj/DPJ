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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import DPJRuntime.*;

/**
 * <code>Vector3f</code> defines a Vector for a three float value tuple.
 * <code>Vector3f</code> can represent any three dimensional value, such as a
 * vertex, a normal, etc. Utility methods are also included to aid in
 * mathematical calculations.
 *
 * @author Mark Powell
 * @author Joshua Slack
 */
public class Vector3f<region R> implements Externalizable {
    private static final long serialVersionUID = 1L;

    /**
     * An array of Vector3f
     */    
    public static arrayclass Array<region R> {
	Vector3f<R> in R;
    }

	/**
     * the x value of the vector.
     */
    public float x in R;

    /**
     * the y value of the vector.
     */
    public float y in R;

    /**
     * the z value of the vector.
     */
    public float z in R;

    /**
     * Constructor instantiates a new <code>Vector3f</code> with default
     * values of (0,0,0).
     *
     */
    public Vector3f() writes R {
        x = y = z = 0;
    }

    public Vector3f(float x, float y, float z) /*writes R*/ {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * <code>set</code> sets the x,y,z values of the vector based on passed
     * parameters.
     *
     * @param x
     *            the x value of the vector.
     * @param y
     *            the y value of the vector.
     * @param z
     *            the z value of the vector.
     * @return this vector
     */
    public Vector3f<R> set(float x, float y, float z) writes R {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * <code>set</code> sets the x,y,z values of the vector by copying the
     * supplied vector.
     *
     * @param vect
     *            the vector to copy.
     * @return this vector
     */
    public <region Rvect> Vector3f<R> 
    set(Vector3f<Rvect> vect) 
    reads Rvect writes R {
        this.x = vect.x;
        this.y = vect.y;
        this.z = vect.z;
        return this;
    }

    /**
     * <code>addLocal</code> adds a provided vector to this vector internally,
     * and returns a handle to this vector for easy chaining of calls. If the
     * provided vector is null, null is returned.
     *
     * @param vec
     *            the vector to add to this vector.
     * @return this
     */
    public <region Rvec> Vector3f<R> 
    addLocal(Vector3f<Rvec> vec) 
    reads Rvec writes R {
        if (null == vec) {
            return null;
        }
        x += vec.x;
        y += vec.y;
        z += vec.z;
        return this;
    }

    /**
     *
     * <code>dot</code> calculates the dot product of this vector with a
     * provided vector. If the provided vector is null, 0 is returned.
     *
     * @param vec
     *            the vector to dot with this vector.
     * @return the resultant dot product of this vector and a given vector.
     */
    public <region Rvec> float 
    dot(Vector3f<Rvec> vec) 
    reads R, Rvec {
        if (null == vec) {
            return 0;
        }
        return x * vec.x + y * vec.y + z * vec.z;
    }

    /**
     * <code>cross</code> calculates the cross product of this vector with a
     * parameter vector v.  The result is stored in <code>result</code>
     *
     * @param v
     *            the vector to take the cross product of with this.
     * @param result
     *            the vector to store the cross product result.
     * @return result, after recieving the cross product vector.
     */
    public <region Rv, Rresult> Vector3f<Rresult> 
    cross(Vector3f<Rv> v, Vector3f<Rresult> result) 
    reads R, Rv writes Rresult {
        return cross(v.x, v.y, v.z, result);
    }

    /**
     * <code>cross</code> calculates the cross product of this vector with a
     * parameter vector v.  The result is stored in <code>result</code>
     *
     * @param otherX
     *            x component of the vector to take the cross product of with this.
     * @param otherY
     *            y component of the vector to take the cross product of with this.
     * @param otherZ
     *            z component of the vector to take the cross product of with this.
     * @param result
     *            the vector to store the cross product result.
     * @return result, after recieving the cross product vector.
     */
    public <region Rresult> Vector3f<Rresult> 
    cross(float otherX, float otherY, float otherZ, Vector3f<Rresult> result) 
    reads R writes Rresult {
        if (result == null) result = new Vector3f<Rresult>();
        float resX = ((y * otherZ) - (z * otherY)); 
        float resY = ((z * otherX) - (x * otherZ));
        float resZ = ((x * otherY) - (y * otherX));
        result.set(resX, resY, resZ);
        return result;
    }

    /**
     * <code>multLocal</code> multiplies this vector by a scalar internally,
     * and returns a handle to this vector for easy chaining of calls.
     *
     * @param scalar
     *            the value to multiply this vector by.
     * @return this
     */
    public Vector3f<region R> multLocal(float scalar) writes R {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }
    
    /**
     * <code>multLocal</code> multiplies a provided vector to this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls. If the provided vector is null, null is returned.
     *
     * @param vec
     *            the vector to mult to this vector.
     * @return this
     */
    public <region Rvec> Vector3f<R> 
    multLocal(Vector3f<Rvec> vec) 
    reads Rvec writes R {
        if (null == vec) {
            return null;
        }
        x *= vec.x;
        y *= vec.y;
        z *= vec.z;
        return this;
    }

    /**
     * <code>mult</code> multiplies this vector by a provided vector,
     * and returns the result in store.
     *
     * @param vec
     *            the vector to mult to this vector.
     * @param store result vector (null to create a new vector)
     * @return this
     */
    public <region Rvec, Rstore> Vector3f<Rstore> 
    mult(Vector3f<Rvec> vec, Vector3f<Rstore> store) 
    reads R, Rvec writes Rstore {
        if (null == vec) {
            return null;
        }
        if (store == null) store = new Vector3f<Rstore>();
        return store.set(x * vec.x, y * vec.y, z * vec.z);
    }

    /**
     *
     * <code>subtract</code>
     *
     * @param vec
     *            the vector to subtract from this
     * @param result
     *            the vector to store the result in
     * @return result
     */
    public <region Rvec, Rresult> Vector3f<Rresult> 
    subtract(Vector3f<Rvec> vec, Vector3f<Rresult> result) 
    reads R, Rvec writes Rresult {
        if(result == null) {
            result = new Vector3f<Rresult>();
        }
        result.x = x - vec.x;
        result.y = y - vec.y;
        result.z = z - vec.z;
        return result;
    }

    /**
     * Check a vector... if it is null or its floats are NaN or infinite,
     * return false.  Else return true.
     * @param vector the vector to check
     * @return true or false as stated above.
     */
    public static <region Rvector> boolean 
    isValidVector(Vector3f<Rvector> vector) 
    reads Rvector {
		if (vector == null) return false;
		if (Float.isNaN(vector.x) ||
		    Float.isNaN(vector.y) ||
		    Float.isNaN(vector.z)) return false;
		if (Float.isInfinite(vector.x) ||
		    Float.isInfinite(vector.y) ||
		    Float.isInfinite(vector.z)) return false;
		return true;
    }


    /**
     * Saves this Vector3f into the given ArrayFloat object.
     * 
     * @param floats
     *            The ArrayFloat to take this Vector3f. If null, a new ArrayFloat(3) is
     *            created.
     * @return The array, with X, Y, Z float values in that order
     */
    public <region Rfloats> ArrayFloat<Rfloats> 
    toArray(ArrayFloat<Rfloats> floats) 
    reads R writes Rfloats:* {
        if (floats == null) {
            floats = new ArrayFloat<Rfloats>(3);
        }
        floats[0] = x;
        floats[1] = y;
        floats[2] = z;
        return floats;
    }

    /**
     * Used with serialization.  Not to be called manually.
     * @param in input
     * @throws IOException
     * @throws ClassNotFoundException
     * @see java.io.Externalizable
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        x=in.readFloat();
        y=in.readFloat();
        z=in.readFloat();
    }

    /**
     * Used with serialization.  Not to be called manually.
     * @param out output
     * @throws IOException
     * @see java.io.Externalizable
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
    }
}
