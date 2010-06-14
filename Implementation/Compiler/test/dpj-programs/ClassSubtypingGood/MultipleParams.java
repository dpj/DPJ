interface A<region R1, R2> {
    void m1() writes R1;
    void m2() writes R2;
}

class B<region R3> implements A<R3, R3> {
    int a in R3;
    
    public void m1() writes R3 {
	a = 1;
    }
    
    public void m2() writes R3 {
	a = 2;
    }
}