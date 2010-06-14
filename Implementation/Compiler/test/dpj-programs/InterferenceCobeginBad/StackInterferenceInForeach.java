class C {
    void m() {
	foreach (int i in 0, 10) {
	    int x = 0;
	    cobegin {
		x = 1;
		x = 2;
	    }
	}
    }
}