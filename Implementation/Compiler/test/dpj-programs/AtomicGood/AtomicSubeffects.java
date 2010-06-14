class C {
    region atomic r1, r2;
    int x1 in r1;
    int x2 in r2;
    void m() writes atomic r1, r2 {
	atomic x1 = 0;
	x2 = 0;
    }
}