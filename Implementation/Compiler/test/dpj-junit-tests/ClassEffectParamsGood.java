import org.junit.Test;

/**
 * These tests verify that class effect params are working in
 * correct cases.
 */

public class ClassEffectParamsGood extends DPJTestCase {

    public ClassEffectParamsGood() {
	super("ClassEffectParamsGood");
    }

    @Test public void testClassEffectParam() throws Throwable {
	compile("DefinitionAndUse");
    }
    
    @Test public void testSubstitutionThroughVariable() throws Throwable {
	compile("SubstitutionThroughVariable");
    }
    
    @Test public void testSubstitutionThroughNewClass() throws Throwable {
	compile("SubstitutionThroughNewClass");
    }
    
    @Test public void testSubstitutionThroughReturnType() throws Throwable {
	compile("SubstitutionThroughReturnType");
    }
    
    @Test public void testSubstitutionThroughInheritance() throws Throwable {
	compile("SubstitutionThroughInheritance");
    }
    
    @Test public void testEffectArgToSuperclass() throws Throwable {
	compile("EffectArgToSuperclass");
    }
    
    @Test public void testInterfaceExtends() throws Throwable {
	compile("InterfaceExtends");
    }
}
