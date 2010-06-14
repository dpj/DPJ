/**
 * Common util functions to find the nearest center point
 * called by Normal.work()
 * 
 * @author Hyojin Sung (sung12@cs.uiuc.edu)
 * @author Rakesh Komuravelli
 *
 */

public class CommonUtil {

    final static float FLT_MAX = 3.40282347E38f;

    /** Find the euclidian distance between the two given points */
    public static float euclidDist2 (float[] pt1,
                                     float[] pt2,
                                     int numdims) reads Root {
        float ans = 0.0f;

        for (int i = 0; i < numdims; i++) {
            ans += (pt1[i] - pt2[i]) * (pt1[i] - pt2[i]);
        }
        return ans;
    }

    /** Find the cluster index for the given point */
    public static int findNearestPoint(float[]   pt,
                                       int       nfeatures,
                                       float[][] clusters,
                                       int       nclusters) reads Root {
        int         index = -1;
        final float limit = 0.99999f;
        float       max_dist = FLT_MAX;

        /* Find the cluster center id with min distance to pt */
        for (int i = 0; i < nclusters; i++) {
            float dist = euclidDist2(pt, clusters[i], nfeatures);
            if ((dist / max_dist) < limit) {
                max_dist = dist;
                index    = i;
                if (max_dist == 0) {
                    break;
                }
            }
        }

        return index;
    }
}