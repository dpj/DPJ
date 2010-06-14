class C {
    region r1, r2, r3;
    int x1 in r1;
    int x2 in r2;
    int x3 in r3;
    // nonint covers everything
    void m() writes nonint r1, nonint r2, nonint r3 {
	nonint x1 = 0;
	atomic x2 = 0;
	x3 = 0;
    }
}