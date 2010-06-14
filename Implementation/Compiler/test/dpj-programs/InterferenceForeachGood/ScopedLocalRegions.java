class C<region R> {
    int x in R;
    void m() {
	foreach (int i in 0, 10) {
	    region r;
	    C<r> c = new C<r>();
	    // Effect on r should be masked outside of loop
	    // and across iterations
	    c.x = i;
	}
    }
}