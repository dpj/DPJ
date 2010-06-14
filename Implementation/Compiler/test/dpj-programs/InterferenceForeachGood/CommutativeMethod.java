class CommutativeMethod {
    commutative void method() writes Root {}
    void testMethod() {
	foreach (int i in 0, 10) {
	    method(); // No interference should be reported here
	}
    }
}