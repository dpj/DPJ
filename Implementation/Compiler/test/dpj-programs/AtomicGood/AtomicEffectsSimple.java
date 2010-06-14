class C {
    region atomic r;
    int x in r;
    void m() {
	cobegin_nd {
	    atomic x = 0;
	    atomic x = 1;
	}
    }
}