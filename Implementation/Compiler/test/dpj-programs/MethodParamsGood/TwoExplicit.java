/** Test (1) two method region parameters, (2) invocation with 
 * explicit region argument
 */
class TwoExplicit<region R1, R2> {
    <region R1, R2>void m1(TwoExplicit<R1, R2> x) {}
    void m2(TwoExplicit<R1, R2> x) {
	this.<region R1, R2>m1(x);
    }
}
