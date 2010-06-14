// Test a bad assignment from a linear array

class C<region R> {
    C<[i]>[]#i A = new C<[i]>[10]#i;
    void bad() {
	// 0 =/= 1
	C<[0]> elt = A[1];	
    }    
}