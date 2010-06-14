/**
 * A body in the BH tree, i.e., a leaf node
 * Adapted from Olden BH by Joshua Barnes et al.
 * @author Robert L. Bocchino Jr.
 * @author Rakesh Komuravelli
 */

public class Body<region R> extends Node {

    /**
     * Velocity of body
     */
    public final Vector<R> vel in R  = new Vector<R>();

    /**
     * Acceleration of body
     */
    public final Vector<R> acc in R  = new Vector<R>();

    /**
     * Indexing for comparing result with the original code
     */
    public int index;

    /**
     * Updated potential at body
     */
    public double phi in R; 

    /**
     * Constructor
     */
    public Body() {
    }
    
    /**
     * Constructor
     * @param body Number of bodies
     */
    public Body(Body<*> body) {
        super(body);
        vel.SETV(body.vel);
        acc.SETV(body.acc);
        phi   = body.phi;
//        cost  = body.cost;
        index = body.index;
    }

    /**
     * Evaluate grav field at a given particle.
     * @param hg Temporary object to hold necessary information during force calculation
     * @param rsize Size of the bounding box referring to the space the bodies span
     * @param root Root of the tree
     */
    <region R1>void hackgrav(HGStruct<R1> hg, double rsize, Node root) 
	 reads MP writes R, R1 {	
        double szsq;
        szsq = rsize * rsize;
        /* recursively scan tree    */
        walksub(root, szsq, Constants.tol*Constants.tol, hg, 0);
        /* stash resulting pot. and */
        phi = hg.phi0;
        /* acceleration in body p   */
        acc.SETV(hg.acc0);
    }

    /**
     * Recursive routine to do hackwalk operation.
     * @param p   pointer into body-tree 
     * @param dsq size of box squared 
     */
    protected <region R1>void walksub(Node p, double dsq, double tolsq, HGStruct<R1> hg,
				      int level) reads MP writes R, R1 {
        /* should p be opened?    */
        if (p.subdivp(p, dsq, tolsq, hg)) {
            /* loop over the subcells */
            for (int k = 0; k < Constants.NSUB; k++) {
                Node r = ((Cell) p).subp[k];
                if (r != null)
                    walksub(r, dsq / 4.0, tolsq, hg, level+1);
            }
        }
        else if (p != (Node) hg.pskip)   {
            gravsub(p, hg);
        }
    }

    /**
     * Compute a single body-body or body-cell interaction.
     * @param p Node of interaction
     * @param hg Temporary object to hold necessary information
     */
    protected <region R1>void gravsub(Node p, HGStruct<R1> hg) 
	reads MP writes R, R1 {
        double drabs, phii, mor3;
        double drsq;

        /* find separation   */
        hg.dr.SUBV(p.pos, hg.pos0);
        /* and square of distance */
        drsq = hg.dr.DOTVP(hg.dr);
        /* use standard softening   */
        drsq += Constants.eps*Constants.eps;
        /* find norm of distance    */
        drabs = Math.sqrt((double) drsq);
        /* and contribution to phi  */
        phii = p.mass / drabs;
        
        /* add to total potential   */
        hg.phi0 -= phii;
        /* form mass / radius qubed */
        mor3 = phii / drsq;
        /* and contribution to acc. */
        hg.ai.MULVS(hg.dr, mor3);
        /* add to net acceleration  */
        hg.acc0.ADDV(hg.acc0, hg.ai);
        
//        cost++;
    }

    /**
     * Cannot subdivide a leaf
     */
    @Override
    protected <region R> boolean subdivp(Node p, double dsq, double tolsq, HGStruct<R> hg) pure {
        return false;
    }

    /**
     * Body implementation just returns mass.
     */
    @Override
    public double hackcofm() {
        return mass;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Body: mass=");
        sb.append(mass);
        sb.append(",");
        sb.append("pos=");
        sb.append(pos);
        return sb.toString();
    }
}
