interface I<effect E> {
    void m() effect E;
}

class C implements I<reads Root> {
    public void m() reads Root {}
}