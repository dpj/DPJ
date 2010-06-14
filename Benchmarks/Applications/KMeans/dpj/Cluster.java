/**
 * 
 * Interface for k-means clustering. called by main.
 * For different number of clusters (min to max),
 * it executes k-means clustering to find the best cluster number.
 * 
 * @author Hyojin Sung (sung12@cs.uiuc.edu)
 * @author Rakesh Komuravelli
 *
 */

public class Cluster {
	
    /** Array of cluser centers */
    private float[][] clusterCentres;
    
    /** Number of clusters */
    private int       bestNclusters;

    /** Getter for cluster centers */
    public float[][] getClusterCentres() {
        return clusterCentres;
    }

    /** Getter for number of clusters */
    public int getBestNclusters() {
        return bestNclusters;
    }

    /** Extract Moments */
    private static float[] extractMoments(float[] data, int num_elts,
                                          int num_moments)
    {
        float[] moments = new float[num_moments];

        for (int i = 0; i < num_elts; i++) {
            moments[0] += data[i];
        }

        moments[0] = moments[0] / num_elts;
        for (int i = 1; i < num_moments; i++) {
            moments[i] = 0;
            for (int j = 0; j < num_elts; j++) {
                moments[i] += Math.pow((data[j]-moments[0]), i+1);
            }
            moments[i] = moments[i] / num_elts;
        }
        return moments;
    }

    /** Zscore Transformation */
    private static void zscoreTransform (Point[] data,
                                         int     numObjects,
                                         int     numAttributes)
    {
        float[] single_variable;
        float[] moments;

        single_variable = new float[numObjects];
        
        for (int i = 0; i < numAttributes; i++) {
            for (int j = 0; j < numObjects; j++) {
                single_variable[j] = data[j].getFeature(i);
            }
            
            moments    = extractMoments(single_variable, numObjects, 2);
            moments[1] = (float) Math.sqrt((double)moments[1]);
            
            for (int j = 0; j < numObjects; j++) {
                data[j].setFeature((data[j].getFeature(i)-moments[0])/moments[1], i);
            }
        }
    }

    /** Find best clusters */
    public int execute (
            int       nthreads,             /* in: number of threads*/
            int       numObjects,           /* number of input objects */
            int       numAttributes,        /* size of attribute of each object */
            Point[]   attributes,
            boolean   use_zscore_transform,
            int       min_nclusters,        /* testing k range from min to max */
            int       max_nclusters,
            float     threshold)            /* in:   */
    {
        int itime;
        int nclusters;

        if (use_zscore_transform) {
            zscoreTransform(attributes, numObjects, numAttributes);
        }

        itime = 0;

        RandomType randomPtr = new RandomType();
        long start = System.nanoTime();

        //From min_nclusters to max_nclusters, find best_nclusters
        for (nclusters = min_nclusters; nclusters <= max_nclusters; nclusters++) {

            randomPtr.random_seed(7);

            Normal work = new Normal(nthreads,
                    attributes,
                    numAttributes,
                    numObjects,
                    nclusters,
                    threshold,
                    randomPtr);
            
            float[][] tmp_cluster_centres = work.execute();

            {
                clusterCentres = tmp_cluster_centres;
                bestNclusters = nclusters;
            }
            itime++;
        } /* nclusters */

        long end = System.nanoTime();
        System.out.println("Overall elapsed time for finding best clusters = " 
                            + (end-start)/1000000000.0);

        return 0;
    }
}