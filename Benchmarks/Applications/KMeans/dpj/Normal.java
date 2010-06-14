/**
 * 
 * Java implementation based on kmeans/normal.c
 * @author Hyojin Sung (sung12@cs.uiuc.edu)
 * @author Rakesh Komuravelli
 *
 */

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Normal {
    
    static float global_delta = 0.0f;
    
    /** Number of threads to run this program on */
    int             nthreads;

    /** Points */
    Point[]         attribute;
    
    /** Number of attributes per point */
    int             nattributes;
    
    /** Number of points */
    int             npoints;
    
    /** Number of clusters under consideration */
    int             nclusters;
    
    /** Threshold until to run the refinement */
    float           threshold;
    
    /** Random value generator */
    RandomType      randomPtr;
    
    /** New cluster centers for every iteration */
    float[][]       new_centers;
    
    /** Number of points in each cluster */
    int[]           globalSize;

    /** Clusters with their centers */
    float[][]       clusters;

    /** Lock per each cluster */
    ReentrantLock[] lock;
    
    /**
     * Constructor
     * @param nthreads
     * @param feature
     * @param nfeatures
     * @param npoints
     * @param nclusters
     * @param threshold
     * @param randomPtr
     */
    Normal(int nthreads, Point[] feature, int nfeatures, int npoints,
            int nclusters, float threshold, RandomType randomPtr) {
        this.nthreads    = nthreads;
        this.attribute   = feature;
        this.nattributes = nfeatures;
        this.npoints     = npoints;
        this.nclusters   = nclusters;
        this.threshold   = threshold;
        this.randomPtr   = randomPtr;

        new_centers = new float[nclusters][nattributes];
        globalSize  = new int[nclusters];
        lock        = new ReentrantLock[nclusters];

        for (int i = 0; i < nclusters; i++) 
            globalSize[i] = 0;
        
        for(int i = 0; i < nclusters; i++) {
            lock[i] = new ReentrantLock();
        }
    }

    /**
     * Reduction function
     */
    commutative void commuteFunc(int clusterIndex, int ptIndex) {
        lock[clusterIndex].lock();

        for(int k = 0; k < nattributes; k++) {
            new_centers[clusterIndex][k] += attribute[ptIndex].getFeature(k);
        }
        
        globalSize[clusterIndex]++;
        
        lock[clusterIndex].unlock();
    }

    /**
     * Work
     */
    void work () {
        float delta = 0.0f;

        long start1 = System.nanoTime();

        foreach (int i in 0, npoints) {

            int index = CommonUtil.findNearestPoint(
                    attribute[i].getFeatures(),
                    nattributes,
                    clusters,
                    nclusters);

            commuteFunc(index, i);
        }

        long end1 = System.nanoTime();
        //System.out.println("per iteration " + (end1-start1)/1000000000.0);
    }

    /**
     * Execute method
     * @return
     */
    float[][] execute()
    {
        int   loop = 0;
        float delta;

        /* Allocate space for returning variable clusters[] */
        clusters = new float[nclusters][nattributes];

        /* Randomly pick cluster centers */
        for (int i = 0; i < nclusters; i++) {
        //foreach (int i in 0, nclusters) {    
            
            int n = (int)(randomPtr.random_generate() % npoints);
            //to test the correctness
            n = nclusters - i - 1;
            for (int j = 0; j < nattributes; j++) {
                clusters[i][j] = attribute[n].getFeature(j);
            }
        }

        do {
            delta = 0.0f;
            global_delta = delta;

            // Find the new cluster centers based on current cluster data
            work();	        

            delta = global_delta;

            // Replace old cluster centers with new_centers 
            for (int i = 0; i < nclusters; i++) {
            //foreach (int i in 0, nclusters) {
                int size = globalSize[i];

                if (size > 0) {
                    for (int j = 0; j < nattributes; j++) { 
                        clusters[i][j] = new_centers[i][j] / size;
                        new_centers[i][j] = 0.0f;
                    }
                }

                // Set the new cluster size
                globalSize[i] = 0;
            }

            delta /= npoints;
        //} while (/*(delta > threshold) && */(loop++ < 500));
        } while (loop++ < 10);

        return clusters;
    }
}