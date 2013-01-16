/**
 * Parallel quadtree construction
 * @author Robert L. Bocchino Jr.
 * @since October 2008, revised June 2010
 */

import DPJRuntime.*;

public class Quadtree extends Harness {

    public abstract class Node<region R> {
	public abstract <region R1>Node<R1> copy() pure;
    }

    private static arrayclass Children<region R> {
	Node<R:[index]> in R:[index];
    }

    public class InnerNode<region R> extends Node<R> {
	final Children<R> children = new Children<R>(4);
	final Box box;
	public InnerNode(Box box) pure {
	    this.box = box;
	}
	public <region R1>Node<R1> copy() pure {
	    return new InnerNode<R1>(this.box);
	}
    }

    public class Body<region R> extends Node<R> {
	public static final int MAX_COORD = 1000000000;
	final int x in R, y in R;
	public Body(int x, int y) pure {
	    this.x = x;
	    this.y = y;
	}
	public <region R1>Node<R1> copy() pure {
	    return new Body<R1>(x,y);
	}
	public String toString() {
	    return "Body (" + x + "," + y + ")";
	}
	@Override
	public boolean equals(Object o) {
	    if (!(o instanceof Body<*>)) return false;
	    Body<*> body = (Body<*>) o;
	    return this.x == body.x && this.y == body.y;
	}
	@Override
	public int hashCode() {
	    return x | y;
	}
    }

    public int randomCoord() pure {
	return (int) (Math.random() * Body.MAX_COORD);
    }
    public Body randomBody() pure {
	return new Body(randomCoord(), randomCoord());
    }

    public class Box {
	final int left, right, top, bottom;
	final int horizontalMid; 
	final int verticalMid;
	public Box(int left, int right, int top, int bottom) pure {
	    this.left = left;
	    this.right = right;
	    this.top = top;
	    this.bottom = bottom;
	    this.horizontalMid = (right + left) / 2;
	    this.verticalMid = (bottom + top) / 2;
	}
	public Box makeQuadrant(int quadrant) pure {
	    int myBottom = (quadrant < 2) ? (bottom + top) / 2 : bottom;
	    int myTop = (quadrant < 2) ? top : (bottom + top) / 2;
	    int myLeft = (quadrant % 2 == 0) ? left : (right + left) / 2;
	    int myRight = (quadrant % 2 == 0) ? (right + left) / 2 : right;
	    return new Box(myLeft, myRight, myTop, myBottom);
	}
	public <region R>int quadrant(Body<R> b) pure {
	    int vertical = (b.y < verticalMid) ? 0 : 2;
	    int horizontal = (b.x < horizontalMid) ? 0 : 1;
	    return vertical + horizontal;
	}
	public String toString() pure {
	    return "Box (left=" + left + ", "
		+ "right=" + right + ", "
		+ "top=" + top + ", "
		+ "bottom=" + bottom + ")";
	}
    }

    private static arrayclass Quadrants<region R1,R2,R3> {
	DPJSequentialHashSet<Body<R1>,R2:[index]> in R3:[index];
    }

    public <region R1,R2>Node<R2> 
	makeTree(DPJSequentialHashSet<Body<R1>,R2> S, Box box, int level)
	writes R2:* 
    {
	// Nothing to add
	if (S.size() == 0) return null;
	// Only one thing
	if (S.size() == 1) return S.iterator().next().<region R2>copy();
	// More than one: Make a new inner node and fill it
	// recursively
	Quadrants<R1,R2,Local> quadrants = 
	    (Quadrants<R1,R2,Local>)
	    ((Object) new DPJSequentialHashSet[4]);
	foreach (int i in 0, 4)
	    quadrants[i] = 
	    new DPJSequentialHashSet<Body<R1>,R2:[i]>();
	for (Body<R1> b : S) {
	    int quadrant =  box.quadrant(b);
	    quadrants[quadrant].add(b);
	}
	InnerNode<R2> node = new InnerNode<R2>(box);
	foreach (int i in 0, 4) {
	    node.children[i] = 
		this.<region R1, R2:[i]>makeTree(quadrants[i], 
						 box.makeQuadrant(i), level+1);
	}
	return node;
    }

    public Quadtree(String[] args) {
	super("Quadtree", args);
    }


    DPJSequentialHashSet<Body> bodies = new DPJSequentialHashSet<Body>();
    Node root;
    @Override
    public void initialize() {
	for (int i = 0; i < size; ++i) {
	    // Should really protect against duplicates here
	    bodies.add(randomBody());
	}
    }

    @Override
    public void runTest() {
	DPJSequentialSet<Body> myBodies = new DPJSequentialHashSet<Body>();
	this.<region Root,Root>checkTree(root, myBodies);
	for (Body b : bodies)
	    assert(myBodies.contains(b));
    }

    // For now, just check that we inserted everything
    private <region R1,R2>void checkTree(Node<R1> subroot, 
					 DPJSequentialSet<Body<R2>> myBodies) 
	{
	if (subroot == null) return;
	else if (subroot instanceof Body<R2>) {
	    myBodies.add((Body<R2>) subroot);
	} else {
	    Children<R1> children = ((InnerNode<R1>) subroot).children;
	    for (int i = 0; i < children.length; ++i) {
		final int j = i;
		Node<R1:[j]> child = children[j];
		this.<region R1:[j],R2>checkTree(child, myBodies);
	    }
	}
    }

    @Override
    public void runWork() {
	root = this.<region Root,Root>makeTree(bodies, 
					       new Box(0, Body.MAX_COORD, 
						       0, Body.MAX_COORD), 0);
    }

    public static void main(String[] args) {
	Quadtree qt = new Quadtree(args);
	qt.run();
    }
}
