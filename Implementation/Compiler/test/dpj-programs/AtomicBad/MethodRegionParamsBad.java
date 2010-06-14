class C {
    region r;
    <region atomic R>void m1() {}
    void m2() {
	// Not allowed: r is not atomic
	this.<region r>m1();
    }
}