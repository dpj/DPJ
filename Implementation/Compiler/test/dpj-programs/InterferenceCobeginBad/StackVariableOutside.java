class StackVariableOutside {
    void m() {
	int x = 0;
	cobegin {
	    x = 1;
	    x = 2;
	}
    }
}