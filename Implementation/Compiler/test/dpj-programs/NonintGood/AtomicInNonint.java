class C {
    region r;
    int x in r;
    void m() writes nonint r {
	nonint {
	    atomic {
		x = 5;
	    }
	}
    }
}