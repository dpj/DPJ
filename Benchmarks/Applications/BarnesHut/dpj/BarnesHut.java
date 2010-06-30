/**
 * Driver class for the Barnes Hut n-body simulation
 * @author Robert L. Bocchino Jr.
 * @author Rakesh Komuravelli
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.concurrent.CyclicBarrier;

public class BarnesHut {

    /**
     * Number of bodies in the simulation
     */
    private final int nbody;

    /**
     * The geometric tree representation of the bodies
     */
    private final Tree tree = new Tree();

    /**
     * Constructor
     */
    public BarnesHut(int nbody) {
        this.nbody  = nbody;
    }

    /**
     * Constructor
     * @param nbody Number of bodies
     * @param nproc Number of threads
     * @param flag Print debug info
     */
    public BarnesHut(int nbody, boolean flag) {
        this.nbody  = nbody;
        this.tree.printBodies = flag;
    }

    /** print usage */
    static void printUsage() {
        System.out.print("Usage:\n");
        System.out.print("dpj BarnesHut <NBODY> [N] [printOutput]\n");
        System.out.print("    where,\n");
        System.out.print("          NBODY is the number of bodies to be simulated\n");
        System.out.print("          second argument is for testing purposes, enter any argument to emit output, say, \"true\"\n");
        System.exit(1);
    }

    /**
     * Program main method
     */
    public static void main(String[] args) throws Exception {
        // Deal with args
        int nbody = 100000;
        boolean emitBodies = false;
        if(args.length < 1 || args.length > 2)
            printUsage();
        if(args.length == 1)
            nbody = Integer.parseInt(args[0]);
        if(args.length == 2) {
            nbody = Integer.parseInt(args[0]);
            emitBodies = true;
        }

	if ((nbody % 32) != 0) {
	    System.err.println("Number of bodies must be divisble by 32!");
	    System.exit(1);
	}

        // Create new BH object
        BarnesHut bh = new BarnesHut(nbody, emitBodies);

        // Initialize the system
        bh.initSystem(nbody);

        // Do the simulation
        bh.doSimulation();
    }

    /**
     * Initialize the system: Create nbody bodies with random mass and
     * position.
     *
     * @param nbody  Number of bodies in the simulation
     */
    public void initSystem(int nbody) throws Exception {
        // Accumulated center of mass
        Vector cmr = new Vector();
        // Accumulated velocity
        Vector cmv = new Vector();

        // Fill in the tree
        tree.rmin.SETVS(-2.0);
        tree.rsize = -2.0 * -2.0;  // t->rmin.elts[0];
        tree.bodies = new Body<[i]>[nbody]#i;

        // Create an array of empty bodies
        for (int i = 0; i < nbody; ++i) {
	    final int j = i;
            tree.bodies[j] = new Body<[j]>();
        }

        // Fill in the bodies, accumulating total mass and velocity.
        // For some reason we are creating 32 distinct groups of
        // bodies, each with its own "seed factor."
        for (int i = 0; i < 32; i++) {
            uniformTestdata(i, cmr, cmv);
        }

        // Normalize coordinates so average pos and vel are 0
        cmr.DIVVS(cmr, (double) nbody);
        cmv.DIVVS(cmv, (double) nbody);
        for (int i = 0; i < tree.bodies.length; ++i) {
	    final int j = i;
            Body<[j]> p = tree.bodies[j];
            p.pos.SUBV(p.pos, cmr); 
            p.vel.SUBV(p.vel, cmv);
            p.index = i;
        }

        // Calculate bounding box once instead of expanding it
        // everytime
        tree.setRsize();
    }

    /**
     * Carry out the simulation
     */
    public void doSimulation() throws InterruptedException {
        double tnow;
        double tout;
        int i, nsteps;

        /* Go through sequence of iterations */
        tnow = 0.0;
        i = 0;
        nsteps = Constants.NSTEPS;
        assert(Util.chatting("About to perform %d iters from %f to %f by %f\n",
                nsteps,tnow,Constants.tstop,Constants.dtime));

        tree.count = 0;
        long start = System.nanoTime();

        i = 0;
        while ((tnow < Constants.tstop + 0.1*Constants.dtime) && (i < Constants.NSTEPS)) {
            tree.stepsystem(0, i); 
            tnow = tnow + Constants.dtime;
            assert(Util.chatting("tnow = %f sp = 0x%x\n", tnow, 0));
            i++;
        }

        long end = System.nanoTime();
        if(!tree.printBodies)
        {
            System.out.println("Overall time taken for force calculation: " + tree.count);
            System.out.print("Overall time taken for entire program: ");
            System.out.println((end-start)/1000000000.0);
        }
    }

    /**
     * Create uniform test data for a segment of tree.bodies.
     *
     * @param nbodyx      Number of bodies to fill in starting at nbodyx *
     *                    segmentNum
     * @param segmentNum  The number of this segment
     * @param cmr         Accumulated center of mass
     * @param cmv         Accumulated velocity
     */
    private void uniformTestdata(int segmentNum, Vector cmr, Vector cmv) {
        double rsc, vsc, r, v, x, y;
        Body<*> p;
        int i;
        int seedfactor = segmentNum+1;
        double temp, t1;
        double seed = 123.0 * (double) seedfactor;
        int k;
        double rsq, rsc1;
        double rad;
        double coeff;
        int nbodyx = nbody/32; 
        double rockmass = 1.0 / (nbody/32.0);
        int start = nbodyx * segmentNum;

        rsc = 3 * Constants.PI / 16;	        /* set length scale factor  */
        vsc = Math.sqrt(1.0 / rsc);		/* and recip. speed scale   */

        for (i = 0; i < nbodyx; i++) {	        /* loop over particles      */
            /* fetch body from previously created array */
            p = tree.bodies[start+i]; 
            //p.mass = 1.0 / nbodyx;			/*   set masses equal       */
            p.mass = rockmass;			/*   set masses equal       */
            seed = Util.rand(seed);
            t1 = Util.xrand(0.0, Constants.MFRAC, seed);
            temp = Math.pow(t1,	                        /*   pick r in struct units */
                    -2.0/3.0) - 1;
            r = 1 / Math.sqrt(temp);

            coeff = 4.0; /* exp(log(nbodyx/DENSITY)/3.0); */
            for (k=0; k < Constants.NDIM; k++) {
                seed = Util.rand(seed);
                r = Util.xrand(0.0, Constants.MFRAC, seed);
                p.pos.elts[k] = coeff*r;
            }

            cmr.ADDV(cmr, p.pos);		        /*   add to running sum     */
            do {					/*   select from fn g(x)    */
                seed = Util.rand(seed);
                x = Util.xrand(0.0, 1.0, seed);   	/*     for x in range 0:1   */
                seed = Util.rand(seed);
                y = Util.xrand(0.0, 0.1, seed);  	/*     max of g(x) is 0.092 */
            } while (y > x*x * Math.pow(1 - x*x, 3.5));	/*   using von Neumann tech */
            v = Math.sqrt(2.0) * 
            x / Math.pow(1 + r*r, 0.25);	        /*   find v in struct units */

            rad = vsc*v;                                /*   pick scaled velocity   */

            do {					/* pick point in NDIM-space */
                for (k = 0; k < Constants.NDIM; k++) {	/* loop over dimensions   */
                    seed = Util.rand(seed);
                    p.vel.elts[k] = 
                        Util.xrand(-1.0, 1.0, seed);	/* pick from unit cube  */
                }
                rsq = p.vel.DOTVP(p.vel);		/*   compute radius squared */
            } while (rsq > 1.0);                	/* reject if outside sphere */
            rsc1 = rad / Math.sqrt(rsq);		/* compute scaling factor   */
            p.vel.MULVS(p.vel, rsc1);		        /* rescale to radius given  */
            cmv.ADDV(cmv, p.vel);	      	        /*   add to running sum     */
        }
    }


}
