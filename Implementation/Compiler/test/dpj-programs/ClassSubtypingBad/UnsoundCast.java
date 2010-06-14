class C<region R> {
    region r1, r2;
    // Should warn about unsound cast
    C<r2> x = (C<r2>) new C<r1>();
}