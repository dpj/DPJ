class LocalRegions<region R> {
    int x in R = 0;
    void method() {
	// Interference here because the same local region is being
	// written in both statements of the cobegin
	region r;
	cobegin {
	    {
		LocalRegions<r> lr1 = new LocalRegions<r>();
		lr1.x = 5;
	    }
	    {
		LocalRegions<r> lr2 = new LocalRegions<r>();
		lr2.x = 10;		
	    }
	}
    }
}