/** Test of incorrect class subtyping with type region params */

class C<type T<region R>> {
    // This may be OK, but it's conservatively disallowed for now
    // Generic type arguments have to match exactly
    C<T<*>> x = new C<T<R>>();
}