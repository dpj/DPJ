class A<region Ra> {
    void m() writes Root : * {
	
    }
}

class B<region Rb> extends A<Rb:[0]> {
    int a in Rb;
    
    void m() writes Rb {
	a = 0;
    }
}
