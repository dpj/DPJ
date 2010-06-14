class StackVariableInside {
    void m() {
	cobegin {
	    int x = 1;
	    x = 2;
	}
    }
}