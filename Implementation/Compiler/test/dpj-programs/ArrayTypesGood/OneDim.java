// One-dimensional array types
class OneDim<region R> {
    // Default RPL
    int[] defaultRPL;
    // Root bound to RPL
    int[]<Root> root;
    // Index-parameterized RPL
    int[]<[i]>#i indexParamRPL;
    // Index-parameterized type
    OneDim<[i]>[]#i indexParamType;
    // Index-parameterized both
    OneDim<[i]>[]<[i]>#i indexParamBoth;   
}