/**
 * Represents a node in the Barnes-Hut tree
 * @author Robert L. Bocchino Jr.
 * @author Rakesh Komuravelli
 */
package DPJBenchmarks;

public abstract class Node {

    /**
     * Region for mass and position data
     */
    region MP;

    /**
     * Total mass of node
     */
    public double mass in MP;

    /**
     * Position of node
     */
    public  Vector<MP> pos in MP = new Vector<MP>();

    /**
     * Cost for cost zone analysis
     */
    //public int cost;

    /**
     * Constructor
     */
    public Node() pure {}

    /**
     * Copy Constructor
     * @param node
     */
    public Node(Node node) {
        this.mass = node.mass;
        this.pos.SETV(node.pos);
    }

    /**
     * Descend tree finding center-of-mass coordinates.
     */
    public abstract double hackcofm();

    /**
     *  Decide if a node should be opened.
     * @param p Node of interest
     * @param dsq
     * @param tolsq
     * @param hg Object holding intermediate computations and other required info
     * @return
     */
    protected abstract <region R> boolean subdivp(Node p, double dsq, 
            double tolsq, HGStruct<R> hg) reads MP writes R;
}
