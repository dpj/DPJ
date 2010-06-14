class A<region R1> {
	A() pure {
		
	}
	
	void m(int j) reads R1 writes this {
		
	}
}

class B<region R2> extends A<R2> {
	int[]<this> a in R2;

	B() pure {
		
	}
	
	void m(int i) reads R2 writes this {
		a[0] = 0;
	}
}