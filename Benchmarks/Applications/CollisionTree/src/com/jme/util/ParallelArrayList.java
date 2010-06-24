package com.jme.util;

/* 
 * Simple wrapper around ArrayList for parallel use.  Only implements
 * operations needed for CollisionTree algorithm.
 */

import java.util.ArrayList;

public class ParallelArrayList<region R>
{
    public ArrayList<Integer> list in R;

    public ParallelArrayList() {
	list = new ArrayList<Integer>();
    }

    public ParallelArrayList(int n) {
	list = new ArrayList<Integer>(n);
    }

    public void add(int val) writes R {
	list.add(val);
    }

    public <region Rb> void addAll(ParallelArrayList<Rb> b) reads Rb writes R {
	list.addAll(b.list);
    }
}
