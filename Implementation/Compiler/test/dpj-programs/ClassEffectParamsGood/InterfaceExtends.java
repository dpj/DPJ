interface I1<effect E1> {
    public void m() effect E1;
}

interface I2<effect E2> extends I1<effect E2> {
    public void m() effect E2;
}

class C implements I2<reads Root> {
    public void m() reads Root {}
}
