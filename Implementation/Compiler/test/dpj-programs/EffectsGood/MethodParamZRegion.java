class A<type T> {
    void m(final T x) writes x {}
}

class B extends A<Object> {
    // Effect is covered by superclass effect
    void m(final Object y) writes y {}
}
