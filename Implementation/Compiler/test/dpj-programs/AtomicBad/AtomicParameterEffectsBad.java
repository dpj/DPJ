class C<region R> {
    int x in R;
    void m() {
	cobegin_nd {
	    // Should warn: R is not atomic
	    atomic x = 0;
	    atomic x = 1;
	}
    }
}