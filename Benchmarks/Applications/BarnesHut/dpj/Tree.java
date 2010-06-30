/**
 * A Barnes Hut force calculation tree
 * Adapted from Olden BH by Joshua Barnes et al.
 * @author Robert L. Bocchino Jr.
 * @author Rakesh Komuravelli
 */

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
public class Tree {

    /**
     * Bounding box for tree
     */
    public Vector rmin = new Vector();
    public double rsize;

    /**
     * Count of time elapsed for force computation
     */
    public float count;

    /**
     * Root of the tree
     */
    public Node root;

    /**
     * Nodes of the tree
     */
    public Body<[i]>[]#i bodies;

    /**
     * Temporary body array required for reordering
     */
    public Body<[i]>[]#i bodiesNew;

    /**
     * No of threads
     */
    int N; 

    /**
     * Flag indicating whether to pring debug information
     */
    boolean printBodies;

    /**
     * Barriers for synchronization
     */
    /*CyclicBarrier barrier;
    CyclicBarrier barMakeTree;
    CyclicBarrier barComputeGrav;
    CyclicBarrier barPosUpdate;
*/
    /**
     * Calculate bounding box once instead of expanding it on every body insertion 
     */
    void setRsize()
    {
        Vector max  = new Vector();
        Vector min  = new Vector();
        double side = 0;
        min.SETVS(Double.MAX_VALUE);
        max.SETVS(Double.MIN_VALUE);
        for(int i = 0; i < bodies.length; i++)
        {
	    final int k = i;
            Body<[k]> p = bodies[k];
            for(int j = 0; j < Constants.NDIM; j++)
            {
                if(p.pos.elts[j] < min.elts[j])
                    min.elts[j] = p.pos.elts[j];
                if(p.pos.elts[j] > max.elts[j])
                    max.elts[j] = p.pos.elts[j];
            }
        }
        max.SUBV(max, min);
        for(int i = 0; i < Constants.NDIM; i++)
        {
            if(side < max.elts[i])
                side = max.elts[i];
        }
        rmin.ADDVS(min, -side/100000.0);
        rsize = 1.00002*side;
    }

    /**
     * Advance N-body system one time-step
     * @param processId process ID
     * @param nstep nth step
     */
    void stepsystem(int processId, int nstep) {
        long start = 0, end = 0;
        // 1. Rebuild the tree with the new positions by processor 0
        if(processId == 0) {
            maketree(nstep);
            start = System.nanoTime();
        }
/*
        //barrier sync
        try {
            barMakeTree.await();
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        catch (BrokenBarrierException ex) {
            ex.printStackTrace();
        }
*/
        // 2. Compute gravity on particles
        computegrav(processId, nstep);
        
        //barrier sync
/*        try {
            barComputeGrav.await();
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        catch (BrokenBarrierException ex) {
            ex.printStackTrace();
        }

        assert(Util.chatting("Done cg\n"));
*/
        // 3. Update positions by processor 0
        //if(processId == 0) {
            end = System.nanoTime();
            count += (end-start)/1000000000.0;
            if(!printBodies)
                System.out.println("timestep " + nstep + " " + (end-start)/1000000000.0);
            vp(nstep);
        //}
        /*try {
            barPosUpdate.await();
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        catch (BrokenBarrierException ex) {
            ex.printStackTrace();
        }*/
        //if(processId == 0)
            setRsize();
    }

    /**
     *  Initialize tree structure for hack force calculation.                     
     */
    void maketree(int step) {
        int[] xqic;
        root = null;
/*        ArrayList<Body> list = new ArrayList<Body>();
        for(int i = 0; i < bodies.length; i++) {
            list.add(bodies[i]);
        }*/
        //root = buildTree(list, Constants.IMAX > 1);
        for (int i = 0; i < bodies.length; ++i) {
	    final int j = i;
            Body<[j]> body = bodies[j];
            // only load massive ones
            if (body.mass != 0.0) {
                // insert into tree
                xqic = intcoord(body);
                root = loadtree(body, xqic, root, Constants.IMAX >> 1, i);
            }
        }
        bodiesNew = new Body<[i]>[bodies.length]#i;

        reOrderBodies(root, 0);
        bodies = bodiesNew;

        if(printBodies)
        {
            for(int i = 0; i < bodies.length; i++)
            {
		final int k = i;
                Body<[k]> p = bodies[k];
//                System.out.print("Cost - " +p.cost+" : ");
                for(int j = 0; j < Constants.NDIM; j++)
                {
                    System.out.printf("%.6f", p.pos.elts[j]);
                    System.out.print(" ");
                }
                System.out.println("");
            }
        }
        assert(Util.chatting("About to hackcofm\n"));
        root.hackcofm();
    }
/*
    Node buildTree(ArrayList<Body> bodyList, int level) {
        if(bodyList.size() == 1)
            return bodyList.get(0);
        //divide the bodyList into 8 lists;
        ArrayList<ArrayList<Body> list = new ArrayList<ArrayList<Body>(8);
        for(int i = 0; i < 8; i++)
        {
            list.add(new ArrayList<Body>());
        }
        for(int i = 0; i < bodyList.size(); i++) {
        //foreach(int i in 0, bodyList.size()) {
            int index;
            index = subindex(intcoord(bodyList.get(i)), level);
            list.get(index).add(bodyList.get(i));
        }
        Node children[] = new Node[8];
        foreach(int i in 0, list.size()) {
            if(list.get(i).size() != 0)
                children[i] = buildTree(list.get(i), level >> 1);
        }
        Cell cell = new Cell();
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).size() != 0)
                cell.subp[i] = children[i];
        }
        return cell;
    }
*/    
    /**
     * Reorder the body array to capture the positioning in the tree
     * @param root
     * @param index
     * @return
     */
    int reOrderBodies(Node root, int index)
    {
        if(root == null)
            return index;
        else if(root instanceof Cell)
        {
            Cell cell = (Cell)root;
            for(int i = 0; i < Constants.NSUB; i++) 
            {
                if(cell.subp[i] == null)
                    continue;
                if(cell.subp[i] instanceof Body)
                {
		    final int j = i;
                    Body<[j]> body = (Body<[j]>)cell.subp[j];
		    final int finalIndex = index;
                    bodiesNew[finalIndex] = new Body<[finalIndex]>(body);
                    //bodiesNew[index] = body;
                    assert(bodiesNew[index]!=null);
                    cell.subp[i] = bodiesNew[index];
                    index++;
                }
                else
                {
                    index = reOrderBodies(cell.subp[i], index);
                }
            }
        }
        return index;
    }

    /**
     * Descend tree and insert particle.
     * @param body - body to be loaded 
     * @param xpic - integer coordinates of p
     * @param level - current level in tree 
     * @param idx - index of body in 
     */
    Node loadtree(Body<*> body, int[] xpic, Node subroot, int level, int idx) {
        if (subroot == null) {
//            bodies[idx] = (Body<[idx]>)body;
            return body;
        }
        /*   dont run out of bits   */
        assert(level != 0);
        Cell cell = null;
        if (subroot instanceof Body) {
            cell = new Cell();
            final int si1 = subindex(intcoord((Body) subroot), level); 
//            Body<[idx]> b = (Body<[idx]>)subroot;
//            bodies[idx] = b;
            cell.subp[si1] = subroot;
        } 
        else {
            assert(subroot instanceof Cell);
            cell = (Cell) subroot;
        }
        final int si = subindex(xpic, level);
        cell.subp[si] = loadtree(body, xpic, cell.subp[si], level >> 1, idx);
        return cell;
    }

    /**
     * Find the sub index into the cell children
     * @param x int coords of the body pos
     * @param l level
     * @return
     */
    int subindex(int[] x, int l) {
        int i, k;
        boolean yes;
        i = 0;
        yes = false;
        if ((x[0] & l) != 0) {
            i += Constants.NSUB >> 1;
            yes = true;
        }
        for (k = 1; k < Constants.NDIM; k++) {
            if ((((x[k] & l) != 0) && !yes)  || ((!((x[k] & l) != 0) && yes))) {
                i += Constants.NSUB >> (k + 1);
                yes = true;
            }
            else
                yes = false;
        }

        return (i);
    }

    /**
     * Create tasks for given number of processes
     */
/*    ArrayList<ArrayList<Body> > createTasks()
    {
        ArrayList<ArrayList<Body> > taskList = new ArrayList<ArrayList<Body> >();
        ArrayList<Body> taskBodies;
        float cavg = (float)root.cost / (float)N;
        int minCost, maxCost;
        for(int m = 0; m < N; m++)
        {
            minCost = (int)(m * cavg);
            maxCost = (int)(cavg * (m+1));
            if(m == (N - 1))
                maxCost++;
            taskBodies = new ArrayList<Body>();
            findMyBodies(root, 0, minCost, maxCost, taskBodies);
            taskList.add(taskBodies);
        }
        return taskList;
    }
*/
    /**
     * Get the bodies for the given process
     */
/*    void findMyBodies(Node cell, int work, int minCost, int maxCost, ArrayList<Body> taskBodies)
    {
        int i;
        Node node;
        Body body;
        Cell inter;
        if(cell instanceof Body)
        {
            body = (Body)cell;
            if(work >= minCost - .1)
            {
                taskBodies.add(body);
            }
            work += body.cost;
        }
        else
        {
            for(i = 0; i < Constants.NSUB && (work < (maxCost - .1)); i++)
            {
                inter = (Cell)cell;
                node = inter.subp[i];
                if(node != null)
                {
                    if((work + node.cost) >= (minCost - .1))
                        findMyBodies(node, work, minCost, maxCost, taskBodies);
                    work += node.cost;
                }
            }
        }
    }
*/
    /**
     * Compute and update forces on particles
     */
    void computegrav(int processId, int nstep) {

        //long start = System.nanoTime();
        
        //for (int i = 0; i < bodies.length; i++) {
        foreach(int i in 0, bodies.length) {
            HGStruct<[i]> hg = new HGStruct<[i]>();
            Vector<[i]> acc1 = new Vector<[i]>();
            Vector<[i]> dacc = new Vector<[i]>();
            Vector<[i]> dvel = new Vector<[i]>();
            double dthf = 0.5 * Constants.dtime;
        
//            bodies[i].cost = 0;
            hg.pskip = bodies[i];
            hg.phi0 = 0;
            hg.pos0.SETV(bodies[i].pos);
            hg.acc0.CLRV();
            acc1.SETV(bodies[i].acc);
            bodies[i].hackgrav(hg, rsize, root);
            if(nstep > 0)
            {
                dacc.SUBV(bodies[i].acc, acc1);
                dvel.MULVS(dacc, dthf);
                bodies[i].vel.ADDV(bodies[i].vel, dvel);
            }
        }

        //long end = System.nanoTime();
        //System.out.println("** " + (end-start)/1000000000.0);
    }


    /**
     * Update the points based on computed forces
     */
    void vp(int nstep) {
                
      long start1 = System.nanoTime();
      for (int i = 0; i < bodies.length; i++) {
      //foreach (int i in 0, bodies.length) {
	  final int j = i;
          Vector dvel = new Vector();
          Vector vel1 = new Vector();
          Vector dpos = new Vector();
          double dthf = 0.5 * Constants.dtime;
          
          dvel.MULVS(bodies[j].acc, dthf);
          vel1.ADDV(bodies[j].vel, dvel);
          dpos.MULVS(vel1, Constants.dtime);
          bodies[j].pos.ADDV(bodies[j].pos, dpos);
          bodies[j].vel.ADDV(vel1, dvel);
        }
      long end1 = System.nanoTime();
      if(!printBodies)
          System.out.println("vp " + (end1-start1)/1000000000.0);
    }

    /**
     * Compute integerized coordinates.
     * Returns: TRUE unless rp was out of bounds.
     */
    public int[] intcoord(Body<*> p) {
        double xsc;
        int[] ic = new int[3];
        boolean inb;
        Vector pos = new Vector();
        pos.SETV(p.pos);

        xsc = (pos.elts[0] - rmin.elts[0]) / rsize;
        if (0.0 <= xsc && xsc < 1.0) 
            ic[0] = 
                (int) Math.floor(Constants.IMAX * xsc);
        else {
            inb = false;
        }

        xsc = (pos.elts[1] - rmin.elts[1]) / rsize;
        if (0.0 <= xsc && xsc < 1.0)
            ic[1] = 
                (int) Math.floor(Constants.IMAX * xsc);
        else {
            inb = false;
        }

        xsc = (pos.elts[2] - rmin.elts[2]) / rsize;
        if (0.0 <= xsc && xsc < 1.0)
            ic[2] = 
                (int) Math.floor(Constants.IMAX * xsc);
        else { 
            inb = false;
        }
        return (ic);
    }
}

