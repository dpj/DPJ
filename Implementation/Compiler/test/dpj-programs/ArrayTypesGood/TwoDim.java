// Two-dimensional array types
class TwoDim<region R> {
    // Default RPL
    int[][] defaultRPL;

    // Root bound to RPL, first only
    int[]<Root>[] rootFirst;
    // Root bound to RPL, second only
    int[][]<Root> rootSecond;
    // Root bound to both
    int[]<Root>[]<Root> rootBoth;
    
    // Index-parameterized RPL first
    int[]<[i]>#i[] indexParamRPLFirst;
    // Index-parameterized RPL second
    int[][]<[i]>#i indexParamRPLSecond;
    // Index-parameterized RPL both
    int[]<[i]>#i[]<[i]> indexParamRPLBoth;
    
    // Index-parameterized type first
    TwoDim<[i]>[]#i[] indexParamTypeFirst;
    // Index-parameterized type second
    TwoDim<[i]>[][]#i indexParamTypeSecond;
    
    // Two indices
    TwoDim<[i]:[j]>[]<[i]>#i[]<[i]:[j]>#j twoIndices;   
}