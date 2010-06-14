class C {
	region r;
	int x in r;
	int y in r;
	void setx(int x) writes r { this.x = x; }
	void sety(int y) writes r { this.y = y; }
	void badness() {
		cobegin { // Should generate warning
			setx(0);
			sety(1);
		}
	}
}
