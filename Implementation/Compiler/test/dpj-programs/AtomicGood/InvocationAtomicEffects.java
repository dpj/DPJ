class Test {
    region atomic r;
    void m1() writes atomic r {}
    void m2() {
        cobegin_nd {
            m1();
            m1();
        }
    }
}
