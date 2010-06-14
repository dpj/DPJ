class C {
    region r;
    int x in r;
    void m() writes nonint r {
	atomic {
	    nonint {
		x = 5;
	    }
	}
    }
}