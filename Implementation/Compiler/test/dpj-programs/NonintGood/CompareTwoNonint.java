class C {
    region r1, r2;
    void m1() writes nonint r1 {}
    void m2() writes nonint r2 {}
    void m3() {
	cobegin {
	    m1();
	    m2();
	}
    }
}