class C {
    region r1, r2;
    int x1 in r1, x2 in r2;
    void m() {
	cobegin_nd {
	    x1 = 0;
	    x2 = 1;
	}
    }
}