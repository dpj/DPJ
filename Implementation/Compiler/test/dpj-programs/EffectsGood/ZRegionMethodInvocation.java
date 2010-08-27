class ZRegionMethodInvocation {
    class Data<region R> {
	void callee() writes R {}
    }

    Data<this> data in this;

    void caller() writes this {
	data.callee();
    }
}

