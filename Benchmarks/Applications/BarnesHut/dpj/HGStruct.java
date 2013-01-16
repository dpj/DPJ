/**
 * Class for temporary storage of computed and required fields for hackgrav
 * @author Rakesh Komuravelli
 */
package DPJBenchmarks;

public class HGStruct<region R> {
    
    /* Node to skip in force evaluation */
    Node pskip in R;
    
    /* point at which to evaluate field */
    Vector<R> pos0 in R;
    
    /* computed potential at pos0       */
    double phi0 in R;
    
    /* computed acceleration at pos0    */
    Vector<R> acc0 in R;
    
    /* intermediate computation for gravsub */
    Vector<R> ai in R;

    /* intermediate computation for gravsub */
    Vector<R> dr in R;
    
    /**
     * Constructor
     */
    public HGStruct() {
        this.pskip = null;
        this.pos0  = new Vector<R>();
        this.phi0  = 0;
        this.acc0  = new Vector<R>();
        this.dr    = new Vector<R>();
        this.ai    = new Vector<R>();
    }
}
