class C1<region R1> {}

class C2<type T, region R2> {
    region r1, r2;
    <region R2>void m1(C2<C1<R2>, R2> x) {}
    void m2() {
	this.m1(new C2<C1<r1>, r2>());
    }
}
