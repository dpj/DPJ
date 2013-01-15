/**
 * Test of array class with index-param'd type
 */
class Data<region R> {
    int val in R;
}

arrayclass DataArray<region R> { 
    Data<R:[index]> in R:[index]; 
}

class IndexParamType {
    region r;
    final int N = 10;
    DataArray<r> a = new DataArray<r>(N);
    void m() {
	foreach (int i in 0, N) {
	    a[i] = new Data<r:[i]>();
	    a[i].val = i;
	}
    }
}
