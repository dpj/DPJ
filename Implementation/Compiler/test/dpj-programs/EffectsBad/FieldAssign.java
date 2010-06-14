class C<region P> {
    region r;
    int y in P;
    // Error:  Read does not cover write
    void m(C<r> x) reads r {
	x.y = 0;
    }
}