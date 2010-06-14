// Test of disjointness constraints when method args are ambiguous

class C<region P> {
    region r;
    static <region P1, P2 | P1#P2>void m() {}
    void warn() {
	// Inferred args are P1=*, P2=*
	// Should warn that disjointness is violated
	m();
    }
}