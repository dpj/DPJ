class C<effect E> {
    C<reads Root> m1() pure { return new C<reads Root>(); }
    void m2() effect E {}
    void m3() reads Root {
	m1().m2();
    }
}