
class A {
    region r;
    int x in r;
}

class B extends A {
    public int m() reads r {
	return x;
    }
}