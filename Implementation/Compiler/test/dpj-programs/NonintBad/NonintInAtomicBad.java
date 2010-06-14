class C {
    region r;
    int x;
    void m() writes atomic r {
	atomic {
	    nonint x = 0;
	}
    }
}