class C<region atomic R> {
    int x in R;
    void m() {
	cobegin_nd {
	    atomic x = 0;
	    atomic x = 1;
	}
    }
}