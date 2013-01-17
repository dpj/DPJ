package DPJRuntime.Framework;

import extra166y.*;
import java.util.ArrayList;
import java.lang.reflect.Array;
import DPJRuntime.Framework.TreeOps.*;

/**
 * <p>class DisjointTree</p>
 * 
 * <p>A linear tree supporting safe recursive parallel operations.
 * Supports several generic parallel operations that can be
 * specialized via hook methods.  Details TBA.
 * T<TR>: type of data in tree nodes
 *   RR : region in which root node of the tree lives.
 **/
public final class DisjointTree<type T<region TR>, region Cont>
    implements DisjointContainer
{
    private Node<T, Cont> root in Cont;
    enum NodeType { LEAFNODE, INNERNODE };
    
    public DisjointTree() pure { this.root = null; }
    
    public Node<T, Cont> getRoot()	reads Cont { return root; }
    
    private static class Node<type T, region DR> {
        protected final NodeType nodeType in DR;
        T data in DR;
        
        public Node(T data, NodeType nt) pure {
            this.data = data;
            this.nodeType = nt;
        }
        public void     setData(T data)  writes DR { this.data = data; }
        public T        getData()        reads DR  { return this.data; }
        public NodeType getNodeType()    reads DR  { return this.nodeType; }
    }
    
    private static class DPJInnerNode<type T<region TR>, region DR>
    extends Node<T, DR> {
        public final ArrayList<Node<T, DR>> children;
        
        // You must know the #children (arity) when creating an inner node.
        public DPJInnerNode(T data, int arity) pure {
            super(data, NodeType.INNERNODE);
            children = new ArrayList<Node<T, DR>>(arity);
            for (int i = 0; i < arity; i++) // needed to allow set(i, V) to work
                children.add(null);
        }
        
        // Get the number of children of this node
	public int arity() reads DR { return children.size(); }
	
	// Get a single child from the array of children.
	public Node<T, DR> get(int i) reads DR {
	    return children.get(i);
	}
	
	// Insert a single child into the array of children.
        // Only to be used inside the framework to ensure linearity.
	public void set(int i, Node<T, DR> node) writes Cont {
	    children.set(i, node);
	}
    }
    
    private static class DPJLeafNode<type T<region TR>, region DR>
    extends Node<T, DR> {
        public DPJLeafNode(T data) pure {
            super(data, NodeType.LEAFNODE);
        }
    }
    
    /**
     * @method buildTree: Build a tree of a fixed arity at all inner nodes.
     * @param  things   : The objects to insert as leaves of the tree.
     * @param  arity    : The fixed arity of all inner nodes in the tree.
     * @param  expander : The logic to choose where to insert each object.
     * @return 
     */
    public <effect E | effect E # writes Cont effect E>
    void buildTree(final DisjointArray<T, Cont> things,
		   final int arity,
		   final TreeOps.NodeExpander<T, effect E> expander)
	writes Cont effect E
    {
	Node<T, Cont> root = null;
	Node<T, Cont> lastNode1 = null, lastNode2 = null;
        for (int i=0; i < things.size(); ++i) {
            T next = things.get(i);
            root = this.<effect E>buildTreeHelper(next, arity, expander,
                                                  root, lastNode2, -1);
            lastNode2 = lastNode1;
            lastNode1 = root;
        }
	this.root = root;
    }
    private <effect E> Node<T, Cont>
    buildTreeHelper(final T thing,
                    final int arity,
                    final TreeOps.NodeExpander<T, effect E> expander,
                    final Node<T, Cont> curNode, 
                    final Node<T, Cont> parentNode,
                    final int indexOfCurNodeInParent) 
	writes Cont effect E
    {
	//--------------------------------------------------------------------
	// 1. If null node, insert immediately as new leaf node.  Return it.
	// 2. If leaf node: Must be non-empty:
	//    (a) Create new InnerNode; this is tree-to-return.
	//    (b) Ask factory for a *fresh* object to go in that node, if any.
	//    (c) Ask which slot to use for old node; insert it in that slot.
	// 3. Else inner node:
	//    (a) This node is tree-to-return.
	//    (b) Ask factory for a *fresh* object to go in that node, if any.
	// 4. Ask which slot to use for new object
	// 5. Recursively build new subtree with updated box.
	//    NOTE: Thing may collide again, but that is the right behavior!
	//          Collisions will continue until tree is expanded enough.
	// 6. Insert new subtree in slot computed in step 4.
	// 7. Return tree-to-return.
	//--------------------------------------------------------------------
	
	// 1. If null node, insert immediately as new leaf node and return it.
	if (curNode == null) {
            DPJLeafNode newNode = new DPJLeafNode(thing);
	    System.out.println("New Node [1]: " + newNode + "; obj =" + thing);
	    return newNode;
	}
        
	// 2. If leaf node: Must be non-empty:
	//    (a) Create new InnerNode; this is node-to-return.
	//    (b) Ask factory for a *fresh* object to go in that node, if any.
	//    (c) Ask which slot to use for old node; insert it in that slot.
        // 
        DPJInnerNode<T, Cont> treeToReturn = null;
        Node<T, Cont> newParent = null;
        if (curNode.nodeType == NodeType.LEAFNODE) {
            assert(curNode.getData() != null);
            T newObject = expander.<region TR>nodeFactory(curNode.getData(),
                                                          parentNode==null? null: parentNode.getData(),
                                                          indexOfCurNodeInParent, thing);
            final DPJInnerNode<T, Cont> newInner =
                new DPJInnerNode<T, Cont>(newObject, arity);
            final int ci = expander.slotToExpand(newObject,
                                                 parentNode==null? null : parentNode.getData(),
                                                 curNode.getData());
            assert(0 <= ci && ci < arity);
            newInner.set(ci, curNode);
            treeToReturn = newInner;
            newParent = parentNode;
        }
        else {
            // 3. Else inner node:
            //    (a) This node is node-to-return.
            //    (b) Ask factory for a *fresh* object to go in that node, if any.
            assert (curNode.nodeType == NodeType.INNERNODE);
            T newObject = expander.<region TR>nodeFactory(curNode.getData(),
                                                          parentNode==null? null : parentNode.getData(),
                                                          indexOfCurNodeInParent, thing);
            curNode.setData(newObject);
            treeToReturn = (DPJInnerNode<T, Cont>) curNode;
            newParent = parentNode;
        }
        
	// 4. Ask which slot to use for new object
        final int cj = expander.slotToExpand(treeToReturn.getData(),
                                             newParent==null? null : newParent.getData(),
                                             thing);
        assert(0 <= cj && cj < arity);
        
	// 5. Recursively call self to build new subtree at child cj,
        //    inserting 'thing' in that subtree.
        final Node<T, Cont> newTree =
            buildTreeHelper(thing, arity, expander, treeToReturn.get(cj), treeToReturn, cj);
        
	// 6. Insert new subtree in slot computed in step 4.
        treeToReturn.set(cj, newTree);
        
	// 7. Return tree-to-return.
        return treeToReturn;
    }
    
    
    /**
     * Entry point for subtree visitor pattern to traverse a subtree
     * in postorder and apply a given operation to each node,
     * passing in the results of the children as a vector Tresult[].
     */
    public static class PostOrderTraversal<type T<region TR>,
	type Tresult<region Rresult>, region Cont> {
	public <effect E | effect E # reads Cont writes TR:*>
	Tresult<Rresult>
	parallelPostorder(Node<T, Cont> subtree,
			  SubtreeVisitorPostOrder<T, Tresult, effect E> visitor)
	    reads Cont writes TR:*, Rresult:* effect E
	{
	    if (subtree == null)
		return null;
	    
	    // recursively visit all the children of subtree
	    ArrayList<Tresult<Rresult>> childResults = null;
            
	    if (subtree.nodeType == NodeType.INNERNODE) {
		// recursively visit the children in parallel
		DPJInnerNode<T, Cont> inner = (DPJInnerNode<T, Cont>) subtree;
		childResults = new ArrayList<Tresult<Rresult>>(inner.arity());
		
                // This is a hack: initialize the slots sequentially.
		for (int i = 0; i < inner.arity(); ++i)
		    childResults.add(null);
                
		foreach (int i in 0, inner.arity()) {
		    childResults.set(i,
				     parallelPostorder(inner.get(i), visitor));
		}
	    }
	    
	    // Now, visit current node, passing in children's result.
	    // Return the result of this postorder visit.
	    Tresult<Rresult> result =
		visitor.<region TR, Rresult>
		visit(subtree.getData(), childResults);
	    return result;
	}
    }
}
