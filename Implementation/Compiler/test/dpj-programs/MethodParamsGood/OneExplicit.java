/** Test (1) single method region parameter, (2) invocation with 
 * explicit region argument
 */
class OneExplicit<region R> {
    <region R>void m1(OneExplicit<R> x) {}
    void m2(OneExplicit<R> x) {
	this.<region R>m1(x);
    }
}
