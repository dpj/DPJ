class C {
	region r;
	int x in r;
	int y in r;
	void m() {
		cobegin { // Should generate warning
			x = 0;
			y = 1;
		}
	}
}
