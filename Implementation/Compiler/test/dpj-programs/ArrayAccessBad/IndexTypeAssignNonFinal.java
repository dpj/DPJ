// Index variable must be declared 'final' for types to match

class C<region R> {
    C<[i]> m(int i, C<[i]>[]#i A) {
	// Error!  Otherwise we'd be returning A[i+1] with a type of A[i]
	++i;
	return A[i];
    }
}