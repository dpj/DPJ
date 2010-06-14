class A {
    region r1, r2;
    int x in r1;
    void m() writes r1 {
	x = 0;
    }
}

class B extends A {
    int y in r2;
    // Error: Can't override 'writes r1' with 'writes r2'
    void m() writes r2 {
	y = 0;
    }
}