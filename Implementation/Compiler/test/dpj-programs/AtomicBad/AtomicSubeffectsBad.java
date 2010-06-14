class C {
    region atomic r;
    int x in r;
    void m() writes atomic r {
	// Effect is not atomic
	x = 0;
    }
}