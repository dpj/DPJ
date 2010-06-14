class C<effect E> {
    void m1() effect E {}
    void m2() reads Root {
	(new C<reads Root>()).m1();
    }
}