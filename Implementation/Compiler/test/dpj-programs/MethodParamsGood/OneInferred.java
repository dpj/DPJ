/** Test (1) single method region parameter, (2) invocation with 
 *  inferred region argument
 */
class OneInferred<region R> {
    <region R>void m1(OneInferred<R> x) {}
    void m2(OneInferred<R> x) {
	this.m1(x);
    }
}
