/**
 * Test interference warning for array class access
 */
arrayclass ArrayInt<region R> {
    int in R;
}

class Interference {
    public static int N = 10;
    void m() {
	region r;
	ArrayInt<r> a = new ArrayInt<r>(N);
	foreach (int i in 0, N)
	    a[i] = i;
    }
}