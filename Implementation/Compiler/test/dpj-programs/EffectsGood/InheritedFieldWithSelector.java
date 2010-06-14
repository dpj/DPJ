class A<region R> {
    int x in R;
}

class B<region R> extends A<R> {}

class C {
    region r;
    B<r> b in r;
    int m() reads r {
        return b.x;
    }
}
