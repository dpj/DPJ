// TODO:  This test has nothing to do with class subtyping!

class A {
    void m() pure {
    }
}

class B<region R> extends A {
    int a in R;
    void m() {
	a = 0;
    }
}