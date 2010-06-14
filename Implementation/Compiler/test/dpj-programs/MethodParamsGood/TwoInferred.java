/** Test (1) two method region parameters, (2) invocation with 
 *  inferred region argument
 */
class TwoInferred<region R1, R2> {
    <region R1, R2>void m1(TwoInferred<R1, R2> x) {}
    void m2(TwoInferred<R1, R2> x) {
	this.m1(x);
    }
}
