class SubstArgsInType<effect E> {
    <effect E1>void m1(SubstArgsInType<effect E1> x) {}
    void m2() {
	this.<writes Root>m1(new SubstArgsInType<writes Root>());
    }
}