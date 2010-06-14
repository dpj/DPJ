class C {
    region r;
    int x in r;
    void m() {
	cobegin_nd {
	    // Should produce warning: r is not atomic
	    atomic x = 0;
	    atomic x = 1;
	}
    }
}