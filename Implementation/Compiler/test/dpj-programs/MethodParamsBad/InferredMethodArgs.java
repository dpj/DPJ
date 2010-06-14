// Test of disjointness constraints for inferred method region arguments

class C<region P> {
    region r;
    static <region P1, P2 | P1#P2>void m(C<P1> x, C<P2> y) {}
    void warn() {
	// Inferred args are P1=r, P2=r
	// Should warn that disjointness is violated
	m(new C<r>(), new C<r>());
    }
}