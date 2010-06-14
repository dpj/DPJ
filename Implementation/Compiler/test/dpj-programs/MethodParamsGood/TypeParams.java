class A<type T> {
    <type T>void m1(A<? super T> x) {}
    void m2() {
        this.<B<Root>>m1(new A<B<Root>>());
    }
}

class B<region R> {}
