/**
 * Method effect annotations
 * 
 * @author Rob Bocchino
 */
public class MethodEffectAnnotations {
    region r1, r2;
    public MethodEffectAnnotations() {}
    public MethodEffectAnnotations(int x) pure {}
    public MethodEffectAnnotations(char x) reads r1, r2 writes r1, r2 {}
    void m1() {}
    void m2() pure {}
    void m3() reads r1, r2 writes r1, r2 {}
    commutative void m4() {}
}
