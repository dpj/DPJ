// Attempt to do unsound array assignment with class element type

class C<region R> {
    // This is unsound ...
    C<*>[] A = new C<R>[10];
    // ... because then we could do this:
    // A[0] = new C<<Root>>();
    //
    // Note that Java generics allow this:
    //
    //  C<?>[] A = new C<T>[10];
    //
    // but they can catch this
    //
    //  A[0] = new C<Object>();
    //
    // as a class cast exception at runtime, whereas
    // our region info is completely erased.
}
