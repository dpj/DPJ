/**
 * Method region parameters
 * 
 * @author Rob Bocchino
 */


class C {
    region r;
    
    /**
     * Basic method region parameter
     */    
    <region P> void m1() {}

    /**
     * Method region parameter under RPL
     */        
    <region P under r> void m2() {}

    /**
     * Two method region parameters declared disjoint
     */            
    <region P1, P2 | P1 # P2> void m3() {}

    /**
     * Method region parameter with type parameter
     */            
    <type T, region P> void m4() {}
    
    /**
     * Invoking method with parameter
     */
    void m5() {
	this.<region r>m1();
    }
    
    /**
     * Constructor with parameters
     */
    <region P>C() {}
    
    /**
     * Invoking the constructor
     */
    void m6() {
        C local = new <region r>C();
    }
}
