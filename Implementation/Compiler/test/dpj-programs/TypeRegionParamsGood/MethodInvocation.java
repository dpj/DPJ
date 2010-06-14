class C<region R> {
    region r;
    <type T<region R>>void m1() writes R {}
    void m2() writes r {
	this.<C<r>>m1();
    }
}