/**
 * 
 * Random number generator
 * (ported from stamp library)
 * 
 * @author Hyojin Sung (sung12@cs.uiuc.edu)
 *
 */

public class RandomType {

	private final static long MATRIX_A = 0x9908b0df;
	private final static long N = 624;
	private final static long M = 397;
	private final static long UPPER_MASK = 0x80000000; /* most significant w-r bits */
	private final static long LOWER_MASK = 0x7fffffff; /* least significant r bits */
	
	private long[] mt;
	private long mti;
	
	RandomType() {
	
		mti = N;
		mt = new long[(int)mti];
		
		init_genrand(0);
	}
	
	private void init_genrand(long seed) {
	   long mti;

	   mt[0]= seed & 0xffffffff;
	    for (mti=1; mti<N; mti++) {
	        mt[(int)mti] =
	          (1812433253 * (mt[(int)mti-1] ^ (mt[(int)mti-1] >> 30)) + mti);
	        /* See Knuth TAOCP Vol2. 3rd Ed. P.106 for multiplier. */
	        /* In the previous versions, MSBs of the seed affect   */
	        /* only MSBs of the array mt[].                        */
	        /* 2002/01/09 modified by Makoto Matsumoto             */
	        mt[(int)mti] &= 0xffffffff;
	        /* for >32 bit machines */
	    }

	    this.mti = mti;
	}
	
	
	public void random_seed(long seed)
	{
	    init_genrand(seed);
	}


	public long random_generate()
	{
		   long y;
		   final long[] mag01 = {0x0, MATRIX_A};
		   
		    /* mag01[x] = x * MATRIX_A  for x=0,1 */
		    if (mti >= N) { /* generate N words at one time */
		        long kk;

		        if (mti == N+1)   /* if init_genrand() has not been called, */
		            init_genrand(5489); /* a default initial seed is used */

		        for (kk=0;kk<N-M;kk++) {
		            y = (mt[(int)kk]&UPPER_MASK)|(mt[(int)kk+1]&LOWER_MASK);
		            mt[(int)kk] = mt[(int)(kk+M)] ^ (y >> 1) ^ mag01[(int)y & 0x1];
		        }
		        for (;kk<N-1;kk++) {
		            y = (mt[(int)kk]&UPPER_MASK)|(mt[(int)kk+1]&LOWER_MASK);
		            mt[(int)kk] = mt[(int)kk+(int)(M-N)] ^ (y >> 1) ^ mag01[(int)y & 0x1];
		        }
		        y = (mt[(int)N-1]&UPPER_MASK)|(mt[0]&LOWER_MASK);
		        mt[(int)N-1] = mt[(int)M-1] ^ (y >> 1) ^ mag01[(int)y & 0x1];
		        mti = 0;
		    }

		    y = mt[(int)mti++];

		    /* Tempering */
		    y ^= (y >> 11);
		    y ^= (y << 7) & 0x9d2c5680;
		    y ^= (y << 15) & 0xefc60000;
		    y ^= (y >> 18);

		    return y;
	}
}