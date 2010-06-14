class C {
	region r1;
	MyPair p in r1;

	// Effect 'writes Root' is missing from summary
	void m() reads Root : r1 {
		p.y = 5;
	}
}

class MyPair {
	int x;
	int y;
}