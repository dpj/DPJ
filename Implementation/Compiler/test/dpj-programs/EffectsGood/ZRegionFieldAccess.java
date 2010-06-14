class A {
    int y in this;
}

class B {
    int m(final A x) reads x {
	return x.y;
    }
}