/** Basic test of using a generic type with a region param */

class C<type T<region R>> {
    T<R> x;
}