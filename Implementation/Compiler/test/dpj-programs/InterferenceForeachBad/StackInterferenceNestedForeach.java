class C {
    void m() {
	foreach (int i in 0, 10) {
	    int x = 0;
	    foreach (int j in 0, 10) {
		x = j; // Should warn about interference!
	    }
	}
    }
}