class C {
    region r1;
    void m1() writes nonint r1 {}
    void m2() writes r1 {}
    void m3() {
	cobegin {
	    m1();
	    m2();
	}
    }
}