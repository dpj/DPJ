interface C1<effect E1> {
    void m1() effect E1;
}

class C2 {
    <effect E2>void m2(C1<effect E2> x) effect E2 {
	x.m1();
    }
}

