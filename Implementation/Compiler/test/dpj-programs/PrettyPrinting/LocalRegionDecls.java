/**
 * Local region declarations
 * 
 * @author Rob Bocchino
 */
public class LocalRegionDecls<region P> {
    public void m() {
	region r1;
	LocalRegionDecls<Local:r1> x;
    }
}
