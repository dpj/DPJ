class MyPair {
	int x;
	int y;
}

class C {
	region r1;
	MyPair p in r1;

	void m() reads Root : r1 {
		p.y = 5;
	}
}