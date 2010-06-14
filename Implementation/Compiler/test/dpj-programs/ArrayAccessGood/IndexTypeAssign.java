// Test subtyping of classes parameterized by [i] regions

class C<region R> {
    C<R:[i]>[]<R:[i]>#i A = new C<R:[i]>[10]<R:[i]>#i;
    void m() {
	final int i = 0;
	A[i] = new C<R:[i]>();
    }
}