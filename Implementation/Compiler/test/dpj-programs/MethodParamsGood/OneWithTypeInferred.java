/** Test (1) single method RPL parameter and single type 
 * parameter, (2) invocation with inferred RPL and type
 * arguments.
 */
class C<type T, region R> {
    <type T1, region R1>void m1(C<T1,R1> x) {}
    void m2(C<T,R> x) {
	m1(x);
    }
}
