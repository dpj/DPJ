class C {
    region r;
    void m1() writes nonint r {}
    void m2() writes nonint r {}
    void m3() {
	cobegin {
	    m1();
	    m2();
	}
    }
}