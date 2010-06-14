class C {
    int x;
    void m() {
	atomic {
	    x = 10;
	}
    }
}