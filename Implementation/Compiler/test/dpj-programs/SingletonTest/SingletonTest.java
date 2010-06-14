class C<type T<region R>> {
    void m() {
        foreach (T<R> elt in null) {}
    }
}
