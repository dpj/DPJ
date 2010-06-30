/**
 * Cell in the BH tree, i.e., a node with children.
 * @author Robert L. Bocchino Jr.
 * @author Rakesh Komuravelli
 */

public class Cell extends Node {
    /**
     * Descendants of cell
     */
    public final Node[]<MP> subp in MP = 
	new Node[Constants.NSUB]<MP>; 

    /**
     * Descend tree finding center-of-mass coordinates.
     */
    @Override
    public double hackcofm() {
        Vector[] tmpv    = new Vector[Constants.NSUB];
        Vector   tmp_pos = new Vector();
        double   mq;
        double[] mrs     = new double[Constants.NSUB];

        mq   = 0.0;
        
        for (int i = 0; i < Constants.NSUB; i++) {
            Node r = subp[i];
            if (r != null) {
                tmpv[i] = new Vector();
                mrs[i] = r.hackcofm();
                /* find moment */
                tmpv[i].MULVS(r.pos, mrs[i]);
            }
        }
        for (int i = 0; i < Constants.NSUB; ++i) {
            /* sum tot. moment */
            if (tmpv[i] != null)
                tmp_pos.ADDV(tmp_pos, tmpv[i]);
            mq = mrs[i] + mq;
            Node r = subp[i];
        }

        mass = mq;
        /* rescale cms position */
        pos.DIVVS(tmp_pos, mass);
        return mq;
    }

    /**
     * Decide if a node should be opened.
     */
    @Override
    protected <region R> boolean subdivp(Node p, double dsq, 
					 double tolsq, HGStruct<R> hg) 
	reads MP writes R {
        double drsq;
        /* compute displacement */   
        hg.dr.SUBV(p.pos, hg.pos0);
        /* and find dist squared */
        drsq = hg.dr.DOTVP(hg.dr);
        /* use geometrical rule */
        return (tolsq * drsq < dsq);
    }
}
