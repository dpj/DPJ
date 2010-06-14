class StackRegions {
    void m() {
	cobegin {
	    // Regions of x and x are noninterfering because the
	    // scopes are different
	    { int x = 5; }
	    { int x = 10; }
	}
    }
}