class C<effect E> {
    C<reads Root> x in Root;
    void m1() effect E {}
    void m2() reads Root {
	x.m1();
    }
}