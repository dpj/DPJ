class DefaultInferredAutoboxing {
	void caller(Integer limit) {
		callee(1);
	}
	
	<region RInternal> void callee(Integer arg) {}
}
