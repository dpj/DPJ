class CommutativeMethod {
    commutative void method(int y) writes Root {}
    int x in Root;
    void testMethod() {
	foreach (int i in 0, 10) {
	    // Interference should be reported here, because of the
	    // separate conflicting read of Root
	    method(x);
	}
    }
}