class C<region R> {
    int x in R;
    void m() {
	region r;
	foreach (int i in 0, 10) {
	    C<r> c = new C<r>();
	    // Effect on r should not be masked outside of loop
	    // or across iterations
	    c.x = i;
	}
    }
}