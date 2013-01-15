/**
 * Test of index-parameterized array class
 */
arrayclass ArrayIPInt<region R> { 
    int in R:[index]; 
}

class IndexParameterized {
    region r;
    final int N = 10;
    ArrayIPInt<r> a = new ArrayIPInt<r>(N);
    void m() {
	foreach (int i in 0, N)
	    a[i] = i;
    }
}
