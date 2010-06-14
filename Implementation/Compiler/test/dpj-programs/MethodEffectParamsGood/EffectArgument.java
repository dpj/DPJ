class C {
    <effect E>void m1() effect E {}
    void m2() pure {
	this.<pure>m1();
    }    
}