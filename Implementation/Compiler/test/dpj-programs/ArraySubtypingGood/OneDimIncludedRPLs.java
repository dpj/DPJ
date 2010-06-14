// One-dimensional array subtyping with equal types
class OneDimIncludedRPLs<region R> {
    // Default RPL
    int[] defaultRPL1;
    int[] defaultRPL2 = defaultRPL1;
    // Root bound to RPL
    int[]<Root> rootRPL1;
    int[]<*> rootRPL2 = rootRPL1;
    // Index-parameterized RPL    
    int[]<[i]>#i indexParamRPL1;
    int[]<*> indexParamRPL2 = indexParamRPL1;
    // Index-parameterized type
    OneDimIncludedRPLs<[i]>[]#i indexParamType1;
    OneDimIncludedRPLs<[i]>[]<*>#i indexParamType2 = indexParamType1;
    // Index-parameterized both
    OneDimIncludedRPLs<[i]>[]<[i]>#i indexParamBoth1;
    OneDimIncludedRPLs<[i]>[]<*>#i indexParamBoth2 = indexParamBoth1;
}