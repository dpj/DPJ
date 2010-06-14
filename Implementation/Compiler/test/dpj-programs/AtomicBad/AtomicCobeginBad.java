class C {
    region atomic r;
    int x in r;
    void m() {
	// Atomic interference should give warning in cobegin
	cobegin {
	    atomic x = 0;
	    atomic x = 1;
	}
    }
}