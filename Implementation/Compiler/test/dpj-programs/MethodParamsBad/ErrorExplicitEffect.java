abstract class ErrorInferEffect {
    class Data<region R> {}
    abstract <region R>void m1() writes R;
    void m2() pure {
	// Explicit effect should be writes Root:*
        this.<region *>m1();
    }
}
