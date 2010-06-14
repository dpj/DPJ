// Test region parameter resolution via substitution for 'this'

class A<region R> {
    A<this> y;
    A<x> m(final A<R> x) {
	return x.y;
    }
}
