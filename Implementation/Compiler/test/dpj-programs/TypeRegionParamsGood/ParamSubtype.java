/** Simple test of subtyping with type region params */

class C<type T<region R>> {
    T<*> m(T<R> x) {
	// * includes R
	return x;
    }
}