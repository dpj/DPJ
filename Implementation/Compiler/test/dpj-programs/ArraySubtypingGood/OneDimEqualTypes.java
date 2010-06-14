// One-dimensional array subtyping with equal types
class OneDimEqualTypes<region R> {
    // Default RPL
    int[] defaultRPL1;
    int[] defaultRPL2 = defaultRPL1;
    // Root bound to RPL
    int[]<Root> rootRPL1;
    int[]<Root> rootRPL2 = rootRPL1;
    // Index-parameterized RPL    
    int[]<[i]>#i indexParamRPL1;
    int[]<[i]>#i indexParamRPL2 = indexParamRPL1;
    // Index-parameterized type
    OneDimEqualTypes<[i]>[]#i indexParamType1;
    OneDimEqualTypes<[i]>[]#i indexParamType2 = indexParamType1;
    // Index-parameterized both
    OneDimEqualTypes<[i]>[]<[i]>#i indexParamBoth1;
    OneDimEqualTypes<[i]>[]<[i]>#i indexParamBoth2 = indexParamBoth1;
}