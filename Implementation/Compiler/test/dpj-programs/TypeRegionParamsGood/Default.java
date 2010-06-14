/** Test default argument for type region params */

class C<type T<region R>> {
    // T == T<R>
    T<R> m(T x) {
	return x;
    }
}