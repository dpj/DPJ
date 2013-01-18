package Tree;

import DPJRuntime.*;

class RecursiveTreeBuild {
    static class Body {
        final double mass, pos;
        Body(double m, double p) { this.mass = m; this.pos = p; }
    }
    static abstract class Node<region R> { Body centerOfMass in R; }
    static class InnerNode<region R> extends Node<R> {
        final double leftBound, rightBound;
        region LeftRgn, RightRgn;
        Node<R:LeftRgn> leftChild in R:LeftRgn;
        Node<R:RightRgn> rightChild in R:RightRgn;
        InnerNode(double lb, double rb) pure { leftBound = lb; rightBound = rb; }
    }
    static class LeafNode<region R> extends Node<R> {
        LeafNode(Body b) pure { centerOfMass = b; }
    }
    private static <region R>int computeSplitPoint(ArraySlice<Body,R> arr, 
                                                   double midpoint) 
        reads R 
    {
        int result = 0;
        // Set result to the first index position in arr whose
        // position is to the right of midpoint
        return result;
    }
    public static <region RN,RA | RN:* # RA:*>Node<RN> 
        makeTree(ArraySlice<Body,RA:*> arr, double leftBound,
                 double rightBound) 
        reads RA:* writes RN:* 
    {
        if (arr.length == 0) return null;
        if (arr.length == 1) return new LeafNode<RN>(arr.get(0));
        double midpoint = (leftBound + rightBound) / 2;
        int splitPoint = computeSplitPoint(arr, midpoint);
        Partition<Body,RA:*> segs = new Partition<Body,RA:*>(arr, splitPoint);
        InnerNode<RN> node = new InnerNode<RN>(leftBound, rightBound);
        cobegin {
            node.leftChild = RecursiveTreeBuild.<region RN:InnerNode.LeftRgn,RA>
                makeTree(segs.get(0), leftBound, midpoint);
            node.rightChild = RecursiveTreeBuild.<region RN:InnerNode.RightRgn,RA>
                makeTree(segs.get(1), midpoint, rightBound);
        }
        return node;
    }
}
