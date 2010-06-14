/**
 * The main KMeans class
 * Takes an ascii file as input and calls the clustering routine
 * 
 * Performs a fuzzy c-means clustering on the data. Fuzzy clustering
 * is performed using min to max clusters and the clustering that gets the best
 * score according to a compactness and separation criterion are returned.
 * 
 * @author Rakesh Komuravelli
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class KMeans {
    
    /** Input file name */
    private String    filename;
    
    /** Cluster object */
    private Cluster   cluster;
    
    /** Min clusters */
    private int       minClusters;
    
    /** Max clusters */
    private int       maxClusters;
    
    /** Input points */
    private Point[]   inpPoints;
    
    /** Number of points */
    private int       numObjs;
    
    /** Number of attributes per point */
    private int       numAttrs;
    
    /** Number of threads */
    private int       nthreads;
    
    /** Whether to use zscore transformation */
    private boolean   useZscore;
    
    /** Threshold */
    private float     threshold;
    
    /** Best number of clusters */
    private int       bestNClusters;
    
    /** Cluster centers */
    private float[][] clusterCenters;
    
    /**
     * Constructor
     */
    public KMeans(String filename, int maxClusters, int minClusters,
                  int nthreads, boolean useZscore, float threshold) {
        this.cluster     = new Cluster();
        this.filename    = filename;
        this.maxClusters = maxClusters;
        this.minClusters = minClusters;
        this.nthreads    = nthreads;
        this.useZscore   = useZscore;
        this.threshold   = threshold;
    }

    /** Usage routine */
    private static void usage() {
        String help = new String("Usage: java KMeans <filename>" +
                                 " <minClusters> <maxClusters> <nthreads>");
        System.out.println(help);
    }
    
    /** Read input data */
    private void readInput() {
        File          inFile = new File(filename);
        StringBuilder data   = new StringBuilder();
        boolean       flag   = false;
        
        //get the number of objects and the number of attributes per object
        try {
            BufferedReader input = new BufferedReader(new FileReader(inFile));
            String line = null;
            try {
                while((line = input.readLine()) != null) {
                    numObjs++;
                    if(!flag)
                    {
                        StringTokenizer tok = new StringTokenizer(line, " \t\n");
                        while(tok.hasMoreElements())
                        {
                            tok.nextToken();
                            numAttrs++;
                        }
                        //do not add the id of the object into numattrs
                        numAttrs--;
                        flag = true;
                    }
                }
            }
            finally {
                input.close();
            }
        }
        catch(IOException ex) {
            ex.printStackTrace();   
        }

        //Get the list of input points
        inpPoints = new Point[numObjs];
        int i = 0;
        int j = 0;
        try {
            BufferedReader input = new BufferedReader(new FileReader(inFile));
            String line = null;
            try {
                while((line = input.readLine()) != null) {
                    StringTokenizer tok = new StringTokenizer(line, " \t\n");
                    tok.nextToken();
                    j = 0;
                    inpPoints[i] = new Point(numAttrs);
                    while(tok.hasMoreElements())
                    {
                        inpPoints[i].setFeature(Float.parseFloat(tok.nextToken()), j);
                        j++;
                    }
                    i++;
                }
            }
            finally {
                input.close();
            }
        }
        catch(IOException ex) {
            ex.printStackTrace();   
        }
    }

    /** Make clusters */
    private void makeClusters() {
        int[]     cluster_assign;
        float[][] cluster_centers;
        
        Point[] attributes = new Point[numObjs];
        
        int nloops = 1;
        //call the clustering method
        for(int loop = 0; loop < nloops; loop++) {
            //copy the input points into attributes
            for(int m = 0; m < numObjs; m++)
                attributes[m] = inpPoints[m].copy();
            
            cluster.execute(nthreads,
                            numObjs,
                            numAttrs,
                            attributes,
                            useZscore,
                            minClusters,
                            maxClusters,
                            threshold);
        }
        
        //get the result: number of clusters and the cluster centers
        clusterCenters = cluster.getClusterCentres();
        bestNClusters  = cluster.getBestNclusters();
    }
    
    /** Dump input */
    public void dumpInput() {
        //dump inputs part1
        System.out.println("number of threads: " + nthreads);
        System.out.println("numObjs: " + numObjs);
        System.out.println("numAttributes: " + numAttrs);
        System.out.println("zscore: " + useZscore);
        System.out.println("max clusters: " + maxClusters);
        System.out.println("min clusters: " + minClusters);
        System.out.println("threshold: " + threshold);
        
        for(int z = 0; z < numObjs; z++)
            System.out.print(inpPoints[z]);
    }
    
    /** Print output */
    public void printResult() {
        //print the result: cluster centers
        for(int m = 0; m < bestNClusters; m++) {
            System.out.print(m + " ");
            for(int n = 0; n < numAttrs; n++) {
                System.out.print(clusterCenters[m][n] + " ");
            }
            System.out.println();
        }                
    }
    
    /** The main function */
    public static void main(String[] args) {
        String fname;
        int    maxClusters;
        int    minClusters;
        int    nthreads = 1;
        int    blockSize;
        
        //5th argument is for emitting debug output
        if(args.length < 4 || args.length > 5) {
            usage();
            System.exit(0);
        }
        
        
        fname = args[0];
        
        if(fname == "")
            usage();
        
        minClusters = Integer.parseInt(args[1]);
        maxClusters = Integer.parseInt(args[2]);
        nthreads    = Integer.parseInt(args[3]);
        
        boolean useZscore = true;
        float   threshold = 0.001f;
        
        KMeans kmeans = new KMeans(fname, maxClusters, minClusters, nthreads,
                                   useZscore, threshold);
        kmeans.readInput();
        //debug
        //kmeans.dumpInput();
        kmeans.makeClusters();
        if(args.length == 5)
            kmeans.printResult();
    }
}
