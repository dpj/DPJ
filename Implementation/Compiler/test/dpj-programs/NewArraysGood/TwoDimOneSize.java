// Creating two-dimensional arrays
class TwoDimOneSize<region R> {
    final int N = 10;

    // Default RPL
    Object defaultRPL = new int[N][];

    // Root bound to RPL, first only
    int[]<Root>[] rootFirst = 
	new int[N]<Root>[];
    // Root bound to RPL, second only
    int[][]<Root> rootSecond = 
	new int[N][]<Root>;
    // Root bound to both
    int[]<Root>[]<Root> rootBoth = 
	new int[N]<Root>[]<Root>;
    
    // Index-parameterized RPL first
    int[]<[i]>#i[] indexParamRPLFirst = 
	new int[N]<[i]>#i[];
    // Index-parameterized RPL second
    int[][]<[i]>#i indexParamRPLSecond = 
	new int[N][]<[i]>#i;
    // Index-parameterized RPL both
    int[]<[i]>#i[]<[i]> indexParamRPLBoth = 
	new int[N]<[i]>#i[]<[i]>;
    
    // Index-parameterized type first
    TwoDimOneSize<[i]>[]#i[] indexParamTypeFirst = 
	new TwoDimOneSize<[i]>[N]#i[];
    // Index-parameterized type second
    TwoDimOneSize<[i]>[][]#i indexParamTypeSecond = 
	new TwoDimOneSize<[i]>[N][]#i;
    
    // Two indices
    TwoDimOneSize<[i]:[j]>[]<[i]>#i[]<[i]:[j]>#j twoIndices = 
	new TwoDimOneSize<[i]:[j]>[N]<[i]>#i[]<[i]:[j]>#j;   
}