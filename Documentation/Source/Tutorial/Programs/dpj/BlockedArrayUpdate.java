import DPJRuntime.*;

class BlockedArrayUpdate {
    public static void main(String[] args) {
        DPJArrayInt array = new DPJArrayInt(100);
        final DPJPartitionInt segs = 
            DPJPartitionInt.stridedPartition(array, 10);
        foreach (int i in 0, segs.length) { 
            DPJArrayInt<segs:[i]:*> seg = segs.get(i);
            for (int j = 0; j < seg.length; ++j) {
                seg.put(j, 10*i+j);
            }
        }
    }
}
