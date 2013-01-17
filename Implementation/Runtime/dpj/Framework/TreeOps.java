package DPJRuntime.Framework;
import java.util.ArrayList;

public class TreeOps {
    /**
     * Building the tree from a linear container:
     */
    public interface NodeExpander<type T<region R>, effect E> {
        /**
         * Method to choose in which slot to insert nextValue in curNode
         * at level 'level' in the tree.
         * This method must return a value i such that
         * 0 <= i < arity which says create a new inner node and insert
         *                as ith child of the new node
         */
        public int slotToExpand(/* final int level, */
                                final T curNodeValue,
                                final T parentNodeValue,
                                final T nextValue) effect E;
        
        /**
         * Method to create a new object for an inner tree node at level 'level'.
         * Region 'R' in T<R> ensures that the object must be a fresh object.
         */
        public <region NR> T<NR> nodeFactory(/* final int level, */
                                             final T curNodeValue,
                                             final T parentNodeValue,
                                             final int indexOfCurNodeInParent,
                                             final T nextValue) effect E;
    }
    
    public interface NodeVisitor<type T<region TR>, effect E> {
        public void visit(final T curNodeValue) writes curNodeValue:*, TR:*
                                                effect E;
    }
    
    public interface SubtreeVisitorPostOrder<type T<region TR>,
					     type V<region VR>, effect E> {
        public <region TR2, region VR2> V<VR2>
	visit(T<TR2> curNodeValue, ArrayList<V<VR2>> childValues)
	    writes TR2, VR2 effect E;
    }
}
