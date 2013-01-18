package Tree;

import DPJRuntime.*;
import Tree.RecursiveTreeBuild.*;

class RecursiveTreeUpdate {
    public static <region R>Body updateCenterOfMass(Node<R> node) 
        writes R:*
    {
        if (node == null) return null;
        if (node instanceof LeafNode<R>)
            return node.centerOfMass;
        Body leftCoM, rightCoM;
        InnerNode<R> innerNode = (InnerNode<R>) node;
        cobegin {
            // Effect is 'writes R:InnerNode.LeftRgn:*'
            leftCoM = updateCenterOfMass(innerNode.leftChild);
            // Effect is 'writes R:InnerNode.RightRgn:*'
            rightCoM = updateCenterOfMass(innerNode.rightChild);
        }
        Body result = null;
        if (leftCoM != null && rightCoM != null)
            result = new Body((leftCoM.mass+rightCoM.mass)/2,
                              (leftCoM.pos+rightCoM.pos)/2);
        else if (leftCoM != null)
            result = new Body(leftCoM.mass, leftCoM.pos);
        else if (rightCoM != null)
            result = new Body(rightCoM.mass, rightCoM.pos);
        innerNode.centerOfMass = result;
        return result;
    }
}
