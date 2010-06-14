class C2<region R2> {
	public int x in R2 = 5;
}

class C1<region R1> {
	region r1;
	C2<r1> c2;

	void m() /* The "reads Root : r1" effect is missing. */ writes Root {
		c2 = new C2<r1>();
		System.out.println(c2.x);
	}
}
