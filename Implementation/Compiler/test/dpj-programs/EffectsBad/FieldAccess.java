class C<region P> {
    region r;
    int y in P;
    // Error:  Should be 'reads r'
    int m(C<r> x) pure {
	return x.y;
    }
    
}