import org.junit.Test;


public class EffectsGood extends DPJTestCase {

    public EffectsGood() {
	super("EffectsGood");
    }
    
    @Test
    public void testInheritedRPL() throws Throwable {
	compile("InheritedRPL");
    }

    @Test
    public void testFlatCallChain() throws Throwable {
	compile("FlatCallChain");
    }

    @Test
    public void testZRegionFieldAccess() throws Throwable {
	compile("ZRegionFieldAccess");
    }

    @Test
    public void testInvokeBeforeDecl() throws Throwable {
	compile("InvokeBeforeDecl");
    }
    
    @Test
    public void testConstructorEffects() throws Throwable {
	compile("ConstructorEffects");
    }
    
    @Test public void testInheritedField() throws Throwable {
	compile("InheritedField");
    }
    
    @Test public void testInheritedFieldWithSelector() throws Throwable {
	compile("InheritedFieldWithSelector");
    }
    
    @Test public void testMethodParamZRegion() throws Throwable {
	compile("MethodParamZRegion");
    }
    
    @Test public void testZRegionArrayAccess() throws Throwable {
	compile("ZRegionArrayAccess");
    }
    
    @Test public void testZRegionMethodInvocation() throws Throwable {
	compile("ZRegionMethodInvocation");
    }
     
    @Test
    public void testStaticInit() throws Throwable {
	compile("StaticInit");
    }
    
    @Test
    public void testStaticFieldInit() throws Throwable {
	compile("StaticFieldInit");
    }

}
