/**
 * Array regions
 * 
 * @author Rob Bocchino
 */

class C<region P1> {
    region r1, r2;
    
    /**
     * Basic array with cells in region r1
     */
    int[]<r1> A1;
    void m1() {
	A1 = new int[10]<r1>;
    }
    
    /**
     * Multidimensional array with cells in regions r1, r2
     */
    int[]<r1>[]<r2> A2;
    void m2() {
	A2 = new int[10]<r1>[]<r2>;
    }
    
    /**
     * Partitioned array
     */
    int[]<r1:[i]>#i A3 in r1;
    void m3() writes r1, r1:[0] {
	A3 = new int[10]<r1:[i]>#i;
	A3[0] = 1;
    }
    
    /**
     * Partitioned array with parameter
     */
    C<r1:[i]>[]<r1:[i]>#i A4 in r1;
    void m4() writes r1, r1:[0] {
	A4 = new C<r1:[i]>[10]<r1:[i]>#i;
        A4[0] = new C<r1:[0]>();
    }    
}
