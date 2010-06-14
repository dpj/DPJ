/** Test (1) single method RPL parameter and single type 
 * parameter, (2) invocation with explicit RPL and type
 * arguments.
 */
class C<type T, region R> {
    <type T, region R>void m1(C<T,R> x) {}
    void m2(C<T,R> x) {
	this.<T,region R>m1(x);
    }
}
