class C {
    <effect E>void m1() effect E {}
    void m2() pure { // Doesn't cover 'writes Root'
	this.<writes Root>m1();
    }
}