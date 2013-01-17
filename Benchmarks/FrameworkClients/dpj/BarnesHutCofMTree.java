import DPJRuntime.Framework.*;
import DPJRuntime.Framework.DisjointTree.*;
import DPJRuntime.Framework.TreeOps.*;
import DPJRuntime.Framework.ArrayOps.*;
import java.util.concurrent.atomic.*;
import java.util.Vector;
import java.util.ArrayList;

/**
 * @author vadve
 * 
 * <p>Simplified 2D version of the center-of-mass computation from the
 * Barnes-Hut n-body algorithm.</p>
 **/
public class BarnesHutCofMTree {
    final static int NBODIES = 3;
    final static int LEFT = 0, RIGHT = 1;
    final static double HORIZON = 7.0;   // right end of space; left end is 0.0
    final static boolean DEBUG = true;

    public static class Body<region R> {
        public double[]<R> pic in R;
        public double mass in R;
        public double cofm in R;
        public double phi in R;
        // Constructor for 2-dimensional space tree (binary tree)
        public Body(int x, double mass, double phi) {
            pic = new double[1]<R>;
            pic[0] = (double) x;
            this.mass = mass;
            this.phi = phi;
        }
	public void print() reads R {
	    System.out.println(": Mass = " + this.mass +
			       ";\tPosition = " + this.pic[0] +
			       ";\tCofM = " + this.cofm);
	}
    }
    
    public static class Cell<region R>
    extends Body<R>		// Superclass represents the CofM
    {
        public double left  in R;      // Left  end of bounding box
        public double right in R;      // Right end of bounding box
        public Cell(double left, double right) {
            super(0, 0.0, 0.0);         // CofM params are set later
            this.left = left;
            this.right = right;
        }
        public double midpoint() reads R {
            return (left + right) / 2;
        }
	public void print() reads R {
	    System.out.println(": left = " + this.left +
			       ";\tright = " + this.right +
			       ";\tCofM = " + this.cofm);
	}
    }
    
    public static class Expander<region R>
    implements NodeExpander<Body<R>, reads R> {
        public int slotToExpand(/* final int level, */
                                final Body<R> curValue,
                                final Body<R> parentValue,
                                final Body<R> valueToInsert)
	reads R {
	    assert(valueToInsert != null);	// else nothing to insert!
	    assert(valueToInsert.pic != null);	// else no data there!
	    assert(curValue != null);		// else no node to insert at!
            
            if (DEBUG) {
                System.out.print("slotToExpand: curValue = ");
                curValue.print();
                System.out.print("slotToExpand: valueToInsert = ");
                valueToInsert.print();
            }
            
            int returnValue;
            if (curValue instanceof Cell) {
                Cell<R> curCell = (Cell<R>) curValue;
                returnValue = (curCell.midpoint() > valueToInsert.pic[0])? LEFT : RIGHT;
            }
            else {
                assert(false);
                assert(curValue instanceof Body);
                returnValue = LEFT;
            }
            
            if (DEBUG) {
                System.out.println("slotToExpand: returning slot = " + returnValue);
            }
            return returnValue;
        }
        
        public <region NR> Body<NR>
        nodeFactory(/* final int level, */
                    final Body<R> curValue,
                    final Body<R> parentValue,
                    final int indexOfCurNodeInParent,
                    final Body<R> valueToInsert)
	reads R {
            if (DEBUG) {
                System.out.print("\n*** nodeFactory: curValue = ");
                curValue.print();
                System.out.print(  "    nodeFactory: parentValue = ");
                if (parentValue != null) {
                    parentValue.print(); System.out.println("");
                }
                else
                    System.out.println("---NULL---\n");
            }
            
            if (parentValue == null || ! (parentValue instanceof Cell))
                return new Cell<NR>(0.0, HORIZON);
            
            assert (parentValue instanceof Cell); // other case handled earlier
            assert(indexOfCurNodeInParent <= 1);	 // binary tree!
            Cell<R> parentCell = (Cell<R>) parentValue;
            
            // newCell gets left half if left child; right half otherwise
            Cell<NR> newCell = (indexOfCurNodeInParent == LEFT)?
                new Cell<NR>(parentCell.left, parentCell.midpoint()) :
                new Cell<NR>(parentCell.midpoint(), parentCell.right);
            
            return newCell;
        }
    }
    
    public static final class BodyGenerator<region R>
    implements DisjointIntAndObjectToObject<Body<R>, Body<R>, pure> {
	// range of body masses is 0..MAXMASS
        final double MAXMASS = 1000.0; // arbitrary units
	
	// Random number generator for values in the range [0.0..1.0].
	// The mass is random*MAXMASS.
        final java.util.Random rstream =
	    new java.util.Random(0); // don't care seed
	
	// Operation to generate each new body.
        public <region R>Body<R> op(int i, final Body<R> /*unused*/obj) pure {
	    assert(obj == null); // should only construct the array once
            double mass = MAXMASS * rstream.nextDouble();
            return new Body<R>(2*i, mass, 0.0);
	}
    }
    
    public static class CofMVisitor<region BHTree>
	implements SubtreeVisitorPostOrder<Body<BHTree>,
		   Vector<Double>, reads BHTree>
    {	
        public <region TR2, region VR2> Vector<Double>
	visit(final Body<TR2> curBody,
	      final ArrayList<Vector<Double>> childValues)
	reads BHTree writes TR2 {
	    Vector<Double> result = new Vector<Double>(2);
            
	    if (childValues == null || childValues.size() == 0) { // leaf node
		curBody.cofm = curBody.pic[0];
		result.add(curBody.mass);
		result.add(curBody.pic[0]);
		return result;
	    }
	    
	    // Binary tree!  Do first child.
	    final Vector<Double> masspos0 = childValues.get(0);
	    double totCofm = curBody.mass * curBody.pic[0] +
		masspos0.get(0) * masspos0.get(1);
	    double totMass = curBody.mass * masspos0.get(0);
	    
	    // Do second child, if any.
	    assert(childValues.size() <= 2);	// binary tree!
	    if (childValues.size() == 2) {
		final Vector<Double> masspos1 = childValues.get(1);
		totCofm += masspos1.get(0) * masspos1.get(1);
		totMass += masspos1.get(0);
	    }
	    
	    curBody.cofm = totCofm / totMass;
            assert(result.size() == 0);
	    result.add(totMass);
	    result.add(totCofm);
	    
	    if (DEBUG) {
		curBody.print();
	    }
	    
            return result;
	}
    }
    
    public static void main(String[] args) {
	region BodyRegion, TreeRegion;

        // First, build a linear array of bodies.
	DisjointArray.Creator creator = new DisjointArray.Creator();
	DisjointArray<Body<BodyRegion>, TreeRegion> bodies =
	    creator.create(NBODIES, Body.class);
	bodies = bodies.<Body<BodyRegion>, pure>
	    withIndexedMapping(new BodyGenerator<BodyRegion>());
	
        // Now insert them into a new tree
        DisjointTree<Body<BodyRegion>, TreeRegion> bhTree = 
            new DisjointTree<Body<BodyRegion>, TreeRegion>();
        bhTree.<reads BodyRegion>buildTree(bodies, /*arity=*/2, 
					   new Expander<BodyRegion>());
	
	// Walk the tree, computing and printing out the CofM
	DisjointTree.PostOrderTraversal<Body<BodyRegion>,
	    Vector<Double>,TreeRegion> walker =
	    new DisjointTree.PostOrderTraversal<Body<BodyRegion>,
	    Vector<Double>,TreeRegion>();
	walker.<reads BodyRegion>parallelPostorder(bhTree.getRoot(),
						   new CofMVisitor<BodyRegion>());
    }
}
