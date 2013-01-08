abstract class ErrorCaptureExplicitArgs {
    class Data<region R> {}
    abstract <region R>void m1(Data<R> x, Data<R> y);
    void m2() {
	region A, B;
	Data<*> x = new Data<A>();
	Data<*> y = new Data<B>();
	// Should not be allowed, otherwise R=A and R=B inside m1
	this.<region *>m1(x,y);
    }
}