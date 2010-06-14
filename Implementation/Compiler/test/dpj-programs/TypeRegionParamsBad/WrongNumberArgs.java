/** Test of error case: wrong number of args to type region params */

class C<type T<region R>> {
    T<R,R> x;
}