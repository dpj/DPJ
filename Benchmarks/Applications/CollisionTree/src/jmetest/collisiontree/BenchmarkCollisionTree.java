package jmetest.collisiontree;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import com.jme.bounding.CollisionTree;
import com.jme.scene.*;
import com.jme.math.*;
import com.jme.util.*;

/**
 * Main class to benchmark the CollisionTree algorithm.  Usage:
 * java BenchmarkCollisionTree input_file cutoff
 */
public class BenchmarkCollisionTree {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		ObjectInputStream in = null;
		int cutoff = Integer.parseInt(args[1]);
		
		try {
			in = new ObjectInputStream(new FileInputStream(args[0]));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		region Rtree1, Rtree2, RLists;
		CollisionTree<Rtree1, Rtree1> tree1;
		CollisionTree<Rtree2, Rtree2> tree2;
		ParallelArrayList<RLists> index1, index2;
		ArrayList<Integer> origIndex1, origIndex2;
		TriMesh<Rtree1> mesh1;
		TriMesh<Rtree2> mesh2;
		long time = 0;
		int iters = 0;
		int origListLen = 0;
		long constructTime = 0;
		
		tree1 = new CollisionTree<Rtree1, Rtree1>();
		tree2 = new CollisionTree<Rtree2, Rtree2>();
		
		try {
			while(true) {
				iters++;
				mesh1 = (TriMesh<Rtree1>)in.readUnshared();
				mesh2 = (TriMesh<Rtree2>)in.readUnshared();

				constructTime -= System.nanoTime();
				tree1.construct(mesh1, false);
				tree2.construct(mesh2, false);
				constructTime += System.nanoTime();
				
				origIndex1 = (ArrayList<Integer>)in.readUnshared();
				origIndex2 = (ArrayList<Integer>)in.readUnshared();
				origListLen = origIndex1.size();
				
				index1 = new ParallelArrayList<RLists>();
				index2 = new ParallelArrayList<RLists>();
		
				time -= System.nanoTime();
				tree1.intersect(tree2, index1, index2, cutoff);
				time += System.nanoTime();

				if (index1.list.size() != origListLen) {
					System.out.println("Error: New results do not match original");
				}
			}
		} catch (EOFException e) {
		    //System.out.println("Total construction time = " + (double)constructTime/1000000000.0 + " seconds");
			System.out.println("Total CollisionTree time = " + (double)time/1000000000.0 + " seconds");
			System.out.println("Number of iterations = " + iters);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
