// Creating two-dimensional arrays
class TwoDimTwoSize<region R> {
    final int N = 10;

    // Default RPL
    int[][] defaultRPL = new int[N][N];

    // Root bound to RPL, first only
    int[]<Root>[] rootFirst = 
	new int[N]<Root>[N];
    // Root bound to RPL, second only
    int[][]<Root> rootSecond = 
	new int[N][N]<Root>;
    // Root bound to both
    int[]<Root>[]<Root> rootBoth = 
	new int[N]<Root>[N]<Root>;
    
    // Index-parameterized RPL first
    int[]<[i]>#i[] indexParamRPLFirst = 
	new int[N]<[i]>#i[N];
    // Index-parameterized RPL second
    int[][]<[i]>#i indexParamRPLSecond = 
	new int[N][N]<[i]>#i;
    // Index-parameterized RPL both
    int[]<[i]>#i[]<[i]> indexParamRPLBoth = 
	new int[N]<[i]>#i[N]<[i]>;
    
    // Index-parameterized type first
    TwoDimTwoSize<[i]>[]#i[] indexParamTypeFirst = 
	new TwoDimTwoSize<[i]>[N]#i[N];
    // Index-parameterized type second
    TwoDimTwoSize<[i]>[][]#i indexParamTypeSecond = 
	new TwoDimTwoSize<[i]>[N][N]#i;
    
    // Two indices
    TwoDimTwoSize<[i]:[j]>[]<[i]>#i[]<[i]:[j]>#j twoIndices = 
	new TwoDimTwoSize<[i]:[j]>[N]<[i]>#i[N]<[i]:[j]>#j;   
}