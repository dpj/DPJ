class C {
	region r;
	int x in r;
	int y in r;
	void m() {
		cobegin { // Should generate warning
			int z = x;
			y = 1;
		}
	}
}
