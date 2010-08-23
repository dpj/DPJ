class SubstArgsInEffect {
    <effect E>void m1() effect E {}
    void m2() writes Root {
	this.<writes Root>m1();
    }
}