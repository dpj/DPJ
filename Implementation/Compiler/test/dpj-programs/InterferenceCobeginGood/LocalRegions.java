class LocalRegions<region R> {
    int x in R = 0;
    void method() {
	// No interference here because the region written in each statement of
	// the cobegin is out of scope in the other statement
	cobegin {
	    {
		region r1;
		LocalRegions<r1> lr1 = new LocalRegions<r1>();
		lr1.x = 5;
	    }
	    {
		region r2;
		LocalRegions<r2> lr2 = new LocalRegions<r2>();
		lr2.x = 10;		
	    }
	}
    }
}