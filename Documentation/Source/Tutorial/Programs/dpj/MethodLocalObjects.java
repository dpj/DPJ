import DPJRuntime.*;

class MethodLocalObjects {
    class LocalObject<region R> {
        int value in R;
        int produceValue(int val) writes R {
            value = val;
            return value;
        }
    }
    <region R>int sumReduce(DPJArrayInt<R> arr) reads R {
        if (arr.length == 0) return 0;
        if (arr.length == 1) return arr.get(0);
        int mid = arr.length/2;
        int left, right;
        cobegin {
            left = sumReduce(arr.subarray(0,mid));
            right = sumReduce(arr.subarray(mid+1,arr.length-mid));
        }
        region LocalRegion;
        LocalObject<LocalRegion> localObject =
            new LocalObject<LocalRegion>();
        // Effect 'writes LocalRegion' is local to method
        int result = localObject.produceValue(left + right);
        return result;
    }
}
