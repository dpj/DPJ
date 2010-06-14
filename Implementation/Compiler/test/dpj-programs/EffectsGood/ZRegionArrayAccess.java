class C<region R> {
    int[]<this> A in this;
    void m(final C<Root> x) writes x {
	x.A[0] = 5;
    }    
}