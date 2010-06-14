/** Test of class subtyping with type region params */

class C<type T<region R>> {
    C<T<R>> x = new C<T<R>>();
}