/** Simple test of subtyping error involving type region params */

class C<type T<region R>> {
    region r1, r2;
    T<r1> m(T<r2> x) {
	// Bad because r1 =/= r2
	return x;
    }
}