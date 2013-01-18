import DPJRuntime.*;

class BlockedArrayUpdate {
    public static void main(String[] args) {
        ArraySliceInt array = new ArraySliceInt(100);
        final PartitionInt segs = 
            PartitionInt.stridedPartition(array, 10);
        foreach (int i in 0, segs.length) { 
            ArraySliceInt<segs:[i]:*> seg = segs.get(i);
            for (int j = 0; j < seg.length; ++j) {
                seg.put(j, 10*i+j);
            }
        }
    }
}
