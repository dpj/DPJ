class C {
    region atomic r;
    int x in r;
    void m1(int x) writes r { this.x = x; }
    void m2() {
	cobegin_nd {
	    atomic m1(0);
	    atomic m1(1);
	}
    }
}