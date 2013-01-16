/**
 * Class for various constants used throughout the program
 * @author Rakesh Komuravelli
 */
package DPJBenchmarks;

public class Constants {
    public static final int    NDIM       = 3;
    public static final int    IMAX_SHIFT = (8 * 4 - 2);
    /* highest bit of int coord */
    public static final int    IMAX       = (1 << (8 * 4 - 2));
    public static final double PI         =  3.14159265358979323846;
    public static final double TWO_PI     =  6.28318530717958647693;
    public static final double FOUR_PI    = 12.56637061435917295385;
    public static final double HALF_PI    =  1.57079632679489661923;
    public static final double FRTHRD_PI  =  4.18879020478639098462;
    /* mass cut off at MFRAC of total */
    public static final double MFRAC      = 0.999;
    /* subcells per cell */
    public static final int    NSUB       = (1 << NDIM);
    /* potential softening parameter            */
    public static final double eps        = 0.05;
    /* accuracy parameter: 0.0 => exact         */
    public static final double tol        = 1.00;
    public static final int    NSTEPS     = 10;
    /* ratio of cells/bodies allocated          */
    public static final double fcells     = 2.0;
    /* timestep for leapfrog integrator */
    public static final double dtime      = 0.025;
    /* time to stop calculation         */
    public static final double tstop      = 2.0;
}