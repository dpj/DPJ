// Creating one-dimensional arrays
class OneDim<region R> {
    final int N = 10;
    // Default RPL
    int[] defaultRPL = new int[N];
    // Root bound to RPL
    int[]<Root> rootRPL = new int[N]<Root>;
    // Index-parameterized RPL
    int[]<[i]>#i indexParamRPL = new int[N]<[i]>#i;
    // Index-parameterized type
    OneDim<[i]>[]#i indexParamType = new OneDim<[i]>[N]#i;
    // Index-parameterized both
    OneDim<[i]>[]<[i]>#i indexParamBoth = new OneDim<[i]>[N]<[i]>#i;   
}