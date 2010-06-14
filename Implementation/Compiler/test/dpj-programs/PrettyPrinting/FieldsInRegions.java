public class FieldsInRegions {
    public region r1, r2, r3;
    public int a in Root:FieldsInRegions.r1, b = 17, c in Root:FieldsInRegions.r2;
    public int a_short in r1, b_short = 17, c_short in r2;
    private float d in Root:FieldsInRegions.r2:FieldsInRegions.r3;
    private float d_short in r3;
    protected FieldsInRegions e in Root:FieldsInRegions.r2:FieldsInRegions.r3 = null;
    protected FieldsInRegions e_short in r3 = null;
}
