class C {
    region r;
    int x in r;
    void m() writes atomic r {
	nonint x = 5;
    }
}