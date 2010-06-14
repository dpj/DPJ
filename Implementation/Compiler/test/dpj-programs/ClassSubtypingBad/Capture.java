// Test case for RPL capture
// From Tech Report, S. 1.5

class C<region P> {
    region r;
    C<P> f in Root;
    void main() {
	C<*> x = new C<Root>();
	// Whoops!  Assigning C<Root:r> to C<Root>
	// Capture conversion should catch this
	x.f = new C<Root : r>();
    }
}
