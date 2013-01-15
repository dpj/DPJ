class A<region RA> {
    int x in RA;  // RPL of x is RA as member of A<<RA>>
}

class B<region RB> extends A<RB> {
    int m() reads RB {
	return x; // RPL of x is RB as member of B<<RB>>
    }
}