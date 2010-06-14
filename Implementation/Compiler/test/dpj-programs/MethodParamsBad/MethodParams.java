// Test of disjointness constraints for method region params

class C {
    region r;
    static <region P1, P2 | P1#P2>void m() {}
    void warn() {
	C.<region r,r>m(); // Should warn that disjointness is violated
    }
}