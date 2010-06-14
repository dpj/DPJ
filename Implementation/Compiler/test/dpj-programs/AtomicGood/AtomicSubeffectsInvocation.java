class C {
    region atomic r;
    int x in r;
    void m1() writes atomic r {
	atomic m2();
    }
    void m2() writes r {
	x = 0;
    }
}