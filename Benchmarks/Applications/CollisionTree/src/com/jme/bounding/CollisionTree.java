package com.jme.bounding;
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
import java.util.ArrayList;

import com.jme.intersection.Intersection;
import com.jme.math.*;
import com.jme.scene.*;
import com.jme.util.*;

import DPJRuntime.*;

/**
 * CollisionTree defines a well balanced red black tree used for triangle
 * accurate collision detection. The CollisionTree supports three types:
 * Oriented Bounding Box, Axis-Aligned Bounding Box and Sphere. The tree is
 * composed of a heirarchy of nodes, all but leaf nodes have two children, a
 * left and a right, where the children contain half of the triangles of the
 * parent. This "half split" is executed down the tree until the node is
 * maintaining a set maximum of triangles. This node is called the leaf node.
 * Intersection checks are handled as follows:<br>
 * 1. The bounds of the node is checked for intersection. If no intersection
 * occurs here, no further processing is needed, the children (nodes or
 * triangles) do not intersect.<br>
 * 2a. If an intersection occurs and we have children left/right nodes, pass the
 * intersection information to the children.<br>
 * 2b. If an intersection occurs and we are a leaf node, pass each triangle
 * individually for intersection checking.<br>
 * Optionally, during creation of the collision tree, sorting can be applied.
 * Sorting will attempt to optimize the order of the triangles in such a way as
 * to best split for left and right sub-trees. This function can lead to faster
 * intersection tests, but increases the creation time for the tree. The number
 * of triangles a leaf node is responsible for is defined in
 * CollisionTreeManager. It is actually recommended to allow
 * CollisionTreeManager to maintain the collision trees for a scene.
 * 
 * @author Mark Powell
 * @see com.jme.bounding.CollisionTreeManager
 */
public class CollisionTree<region R, RMesh> {
// R is specific to this node.
// RMesh is for structures shared by all nodes in this tree (mesh & triIndex)
// Root nodes of collision trees should have R == RMesh

    // Regions for left, right subtrees
    static region Left, Right;
    
    // children trees
    protected CollisionTree<R:Left, RMesh> left in R:Left;
    protected CollisionTree<R:Right, RMesh> right in R:Right;

    // bounding volumes that contain the triangles that the node is
    // handling
    protected BoundingVolume<R> bounds in R;

    // the list of triangle indices that compose the tree. This list
    // contains all the triangles of the mesh and is shared between
    // all nodes of this tree.
    protected ArrayInt<RMesh> triIndex in RMesh;

    // Defines the pointers into the triIndex array that this node is
    // directly responsible for.
    protected int start in R, end in R;

    // Required Spatial information
    protected TriMesh<RMesh> mesh in RMesh;
    
    public static final int maxTrisPerLeaf = 16;

    /**
     * Constructor creates a new instance of CollisionTree.
     * 
     * @param type
     *            the type of collision tree to make
     * @see Type
     */
    public CollisionTree() {
    }
    
    /**
     * This constructor creates a copy of t with appropriate hierarchical region typing
     * @param t
     */
    public CollisionTree(CollisionTree<*, RMesh> t)
    /*writes R, R:Left, R:Right, RMesh*/
    {
    	left = t.left == null ? null : new CollisionTree<R:Left, RMesh>(t.left);
    	right = t.right == null ? null : new CollisionTree<R:Right, RMesh>(t.right);
    	
    	bounds = new BoundingBox<R>((BoundingBox)t.bounds);
    	
    	triIndex = t.triIndex;
    	start = t.start;
    	end = t.end;
    	mesh = t.mesh;
    }

    /**
     * Recreate this Collision Tree for the given mesh.
     * 
     * @param mesh
     *            The trimesh that this OBBTree should represent.
     * @param doSort
     *            true to sort triangles during creation, false otherwise
     */
    public void construct(TriMesh<RMesh> mesh, boolean doSort) {
        this.mesh = mesh;
        triIndex = mesh.getTriangleIndices(triIndex);
        createTree(0, triIndex.length, doSort);
    }

    /**
     * Creates a Collision Tree by recursively creating children nodes,
     * splitting the triangles this node is responsible for in half until the
     * desired triangle count is reached.
     * 
     * @param start
     *            The start index of the tris array, inclusive.
     * @param end
     *            The end index of the tris array, exclusive.
     * @param doSort
     *            True if the triangles should be sorted at each level, false
     *            otherwise.
     */
    public void createTree(int start, int end, boolean doSort) {
        this.start = start;
        this.end = end;

        if (triIndex == null) {
            return;
        }

        createBounds();

        // the bounds at this level should contain all the triangles this level
        // is reponsible for.
        bounds.computeFromTris(triIndex, mesh, start, end);

        // check to see if we are a leaf, if the number of triangles we
        // reference is less than or equal to the maximum defined by the
        // CollisionTreeManager we are done.
        if (end - start + 1 <= maxTrisPerLeaf) {
        	left = null;
        	right = null;
            return;
        }

        // if doSort is set we need to attempt to optimize the referenced
        // triangles.
        // optimizing the sorting of the triangles will help group them
        // spatially
        // in the left/right children better.
        if (doSort) {
        	System.err.println("Tried to sort tris -- not supported");
            //sortTris();
        }

        // create the left child
        if (left == null) {
            left = new CollisionTree<R:Left, RMesh>();
        }

        left.triIndex = this.triIndex;
        left.mesh = this.mesh;
        left.createTree(start, (start + end) / 2, doSort);

        // create the right child
        if (right == null) {
            right = new CollisionTree<R:Right, RMesh>();
        }
        right.triIndex = this.triIndex;
        right.mesh = this.mesh;
        right.createTree((start + end) / 2, end, doSort);
    }    

    /**
     * creates the appropriate bounding volume based on the type set during
     * construction.
     */
    private void createBounds() {
    	if (bounds == null) {
    		bounds = new BoundingBox<R>();
    	}
    }
    
    public static <region R1, R2> boolean 
    intersectsBounding_r(BoundingVolume<R1> myWorldBounds, BoundingVolume<R2> volume) 
    reads R1, R2 
    {
        return myWorldBounds.intersectsBoundingBox((BoundingBox<R2>)volume);
    }
    
    public <region R_cT, RLists | R_cT:* # RLists, R:* # RLists, RMesh:* # RLists> boolean 
    intersect(CollisionTree<R_cT, R_cT> collisionTree,
            ParallelArrayList<RLists> aList, ParallelArrayList<RLists> bList, int cutoff) 
    {
        if (collisionTree == null) {
            return false;
        }
    	
    	region RTemps, RmyWB;
    	
    	Matrix3f<RTemps> tempMat = new Matrix3f<RTemps>(); 
    	Vector3f<RTemps> tempVa = new Vector3f<RTemps>();
    	Vector3f<RTemps> tempVb = new Vector3f<RTemps>();
    	
        // Generate myWorldBounds
        BoundingBox<RmyWB> worldBounds = bounds.<region RMesh, RMesh, RMesh, RmyWB, RTemps, RTemps, RTemps>
        	transform_r(mesh.getWorldRotation(), mesh.getWorldTranslation(), mesh.getWorldScale(), null, tempMat, tempVa, tempVb);

    	return this.<region R_cT, R_cT, RTemps, RLists, RmyWB>intersect(collisionTree, aList, bList, 
    			worldBounds, cutoff, null, null, null);
    }

    /**
     * Determines if this Collision Tree intersects the given CollisionTree. If
     * a collision occurs, true is returned, otherwise false is returned. If the
     * provided collisionTree is invalid, false is returned. All collisions that
     * occur are stored in lists as an integer index into the mesh's triangle
     * buffer. where aList is the triangles for this mesh and bList is the
     * triangles for the test tree.
     * 
     * @param collisionTree
     *            The Tree to test.
     * @param aList
     *            a list to contain the colliding triangles of this mesh.
     * @param bList
     *            a list to contain the colliding triangles of the testing mesh.
     * @return True if they intersect, false otherwise.
     */
    public
    <region R_cT, RctMesh, RTemps, RLists, Rwb | 
    	RLists # R:*,   RLists # R_cT:*,   RLists # Rwb,   RLists # RMesh:*,   RLists # RctMesh:*, 
    	RTemps:* # R:*, RTemps:* # R_cT:*, RTemps:* # Rwb, RTemps:* # RMesh:*, RTemps:* # RctMesh:*> 
    boolean 
    intersect(CollisionTree<R_cT, RctMesh> collisionTree,
            ParallelArrayList<RLists> aList, ParallelArrayList<RLists> bList, 
            BoundingVolume<Rwb> myWorldBounds, int cutoff,
            Matrix3f<RTemps> tempMat, Vector3f<RTemps> tempVa, Vector3f<RTemps> tempVb) 
    reads R:*, R_cT:*, Rwb, RMesh:*, RctMesh:*
    writes RTemps:*, RLists
    {
        // Temporaries for BoundingBox.transform_r (vectors also reused later)
        if (tempMat == null) {
        	tempMat = new Matrix3f<RTemps>(); 
        	tempVa = new Vector3f<RTemps>();
        	tempVb = new Vector3f<RTemps>();
        }

        // our two collision bounds do not intersect, therefore, our triangles
        // must not intersect. Return false.
        region R_cTWB;
        BoundingVolume<R_cTWB> ctWorldBounds = collisionTree.bounds.<region RctMesh, RctMesh, RctMesh, R_cTWB, RTemps, RTemps, RTemps>
        		transform_r(collisionTree.mesh.getWorldRotation(), collisionTree.mesh.getWorldTranslation(), 
        					collisionTree.mesh.getWorldScale(), null, tempMat, tempVa, tempVb);

        if (!intersectsBounding_r(myWorldBounds, ctWorldBounds)) {
            return false;
        }

        region RTemps2, RLists2;
        
        // if our node is not a leaf send the children (both left and right) to
        // the test tree.
        if (left != null) { // This is not a leaf
        	if (cutoff > 0) {
        		boolean test1, test2;

        		ParallelArrayList<RLists2> aList2 = new ParallelArrayList<RLists2>();
        		ParallelArrayList<RLists2> bList2 = new ParallelArrayList<RLists2>();

	        	cobegin {
	        		test1 = collisionTree./*<region R:Left, RMesh, RTemps, RLists, R_cTWB>*/
	        				intersect(left, bList, aList, ctWorldBounds, cutoff-1, tempMat, tempVa, tempVb);
	        		test2 = collisionTree.<region R:Right, RMesh, RTemps2, RLists2, R_cTWB>
	        				intersect(right, bList2, aList2, ctWorldBounds, cutoff-1, null, null, null);
	        	}
	        	
	            if (test2) {
	            	aList.addAll(aList2);
	            	bList.addAll(bList2);
	            	return true;
	            } else if (test1) {
	            	return true;
	            } else {
	            	return false;
	            }
        	} else {
                boolean test = collisionTree.intersect(left, bList, aList, ctWorldBounds, 0, tempMat, tempVa, tempVb);
                test = collisionTree.intersect(right, bList, aList, ctWorldBounds, 0, tempMat, tempVa, tempVb) || test;
                return test;
        	}
        }

        // This node is a leaf, but the testing tree node is not. Therefore,
        // continue processing the testing tree until we find its leaves.
        if (collisionTree.left != null) {
        	if (cutoff > 0) {
	        	boolean test1, test2;
	        	
	        	ParallelArrayList<RLists2> aList2 = new ParallelArrayList<RLists2>();
	        	ParallelArrayList<RLists2> bList2 = new ParallelArrayList<RLists2>();
	        	
	        	cobegin {
		            test1 = this./*<region R_cT:Left, RctMesh, RTemps, RLists, Rwb>*/
		            		intersect(collisionTree.left, aList, bList, myWorldBounds, cutoff-1, tempMat, tempVa, tempVb);
		            test2 = this.<region R_cT:Right, RctMesh, RTemps2, RLists2, Rwb>
		            		intersect(collisionTree.right, aList2, bList2, myWorldBounds, cutoff-1, null, null, null);
	        	}
	
	            if (test2) {
	            	aList.addAll(aList2);
	            	bList.addAll(bList2);
	            	return true;
	            } else if (test1) {
	            	return true;
	            } else {
	            	return false;
	            }
        	} else {
                boolean test = this.intersect(collisionTree.left, aList, bList, myWorldBounds, 0, tempMat, tempVa, tempVb);
                test = this.intersect(collisionTree.right, aList, bList, myWorldBounds, 0, tempMat, tempVa, tempVb) || test;
                return test;        		
        	}
        }

        // both this node and the testing node are leaves. Therefore, we can
        // switch to checking the contained triangles with each other. Any
        // that are found to intersect are placed in the appropriate list.
        Quaternion<RMesh> roti = mesh.getWorldRotation();
        Vector3f<RMesh> scalei = mesh.getWorldScale();
        Vector3f<RMesh> transi = mesh.getWorldTranslation();

        Quaternion<RctMesh> rotj = collisionTree.mesh.getWorldRotation();
        Vector3f<RctMesh> scalej = collisionTree.mesh.getWorldScale();
        Vector3f<RctMesh> transj = collisionTree.mesh.getWorldTranslation();

        boolean test = false;

        // Temporaries to contain information for ray intersection
        // Converted from fields to provide thread-safety
        final Vector3f<RTemps> tempVc = new Vector3f<RTemps>();
        final Vector3f<RTemps> tempVd = new Vector3f<RTemps>();
        final Vector3f<RTemps> tempVe = new Vector3f<RTemps>();
        final Vector3f<RTemps> tempVf = new Vector3f<RTemps>();

        Vector3f<RTemps>[]<RTemps> verts = new Vector3f<RTemps>[3]<RTemps>;
        Vector3f<RTemps>[]<RTemps> target = new Vector3f<RTemps>[3]<RTemps>;
        
        // Temporaries for Intersection.intersection_r
		Vector3f<RTemps> e1 = new Vector3f<RTemps>();
		Vector3f<RTemps> e2 = new Vector3f<RTemps>();
		Vector3f<RTemps> n1 = new Vector3f<RTemps>();
		Vector3f<RTemps> n2 = new Vector3f<RTemps>();	
		ArrayFloat<RTemps> isect1 = new ArrayFloat<RTemps>(2);
		ArrayFloat<RTemps> isect2 = new ArrayFloat<RTemps>(2);
 
        for (int i = start; i < end; i++) {
            mesh.<region RTemps>getTriangle(triIndex[i], verts);
            roti.mult(tempVa.set(verts[0]).multLocal(scalei), tempVa).addLocal(transi);
            roti.mult(tempVb.set(verts[1]).multLocal(scalei), tempVb).addLocal(transi);
            roti.mult(tempVc.set(verts[2]).multLocal(scalei), tempVc).addLocal(transi);
            for (int j = collisionTree.start; j < collisionTree.end; j++) {
                collisionTree.mesh.<region RTemps>getTriangle(collisionTree.triIndex[j], target);
                rotj.mult(tempVd.set(target[0]).multLocal(scalej), tempVd).addLocal(transj);
                rotj.mult(tempVe.set(target[1]).multLocal(scalej), tempVe).addLocal(transj);
                rotj.mult(tempVf.set(target[2]).multLocal(scalej), tempVf).addLocal(transj);
                if (Intersection.intersection_r(tempVa, tempVb, tempVc, tempVd, tempVe, tempVf, e1, e2, n1, n2, isect1, isect2)) {
                    test = true;
                    aList.add(triIndex[i]);
                    bList.add(collisionTree.triIndex[j]);
                }
            }
        }
        
        return test;

    }
}
