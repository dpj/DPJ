class C {
    int x;
    void m1(int x) writes Root { this.x = x; }
    void m2() {
	cobegin_nd {
	    // Missing atomic
	    m1(0);
	    // Missing atomic
	    m1(1);
	}
    }
}