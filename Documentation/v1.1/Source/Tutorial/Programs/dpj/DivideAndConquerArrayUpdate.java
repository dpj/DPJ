import DPJRuntime.*;

class DivideAndConquerArrayUpdate {
    static <region R>void recursiveInit(ArraySliceInt<R> arr, 
                                        int val, 
                                        int sequentialSize)
      writes R:* {
        if (arr.length <= sequentialSize) {
            // Base case: sequential
            for (int i = 0; i < arr.length; ++i)
                arr.put(i, val);
        }
        // Recursive case: parallel
        int mid = arr.length / 2;
        final PartitionInt<R> segs = 
            new PartitionInt<R>(arr, mid);
        cobegin {
            // Effect is 'writes segs:[0]:*'
            recursiveInit(segs.get(0), val, sequentialSize);
            // Effect is 'writes segs:[1]:*'
            recursiveInit(segs.get(1), val, sequentialSize);        
        }
    }
}
