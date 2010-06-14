class C1 {
	region r1, r3;
	int x in r1;
	C2 c2 in r3;
	
	C2 m1() writes Root : r1, Root : r3 {
		x = 1;
		c2 = new C2();
		return c2;
	}
}

class C2 {
	region r2;
	int y in r2;
	
	void m2() writes Root : r2 {
		y = 2;
	}
}

class C3 {
	void m3() writes Root : C1.r1, Root : C2.r2, Root : C1.r3 {
		C1 c1 = new C1();
		c1.m1().m2();
	}
}