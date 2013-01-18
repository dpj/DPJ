class IterationLocalObjects {
    class LocalObject<region R> {
        int value in R;
        int produceValue(int val) writes R {
            value = val;
            return value;
        }
    }
    int[]<[_]> results = new int[10]<[_]>;
    void usingLocalRegions() {
        foreach (int i in 0, 10) {
            region LocalRegion;
            LocalObject<LocalRegion> localObject = 
                new LocalObject<LocalRegion>();
            // Effect 'writes LocalRegion' is local to an iteration
            results[i] = localObject.produceValue(i);
        }
    }
    void usingArrayRegions() {
        foreach (int i in 0, 10) {
            LocalObject<[i]> localObject = 
                new LocalObject<[i]>();
            // Effect 'writes [i]' is local to an iteration
            results[i] = localObject.produceValue(i);
        }
    }
}
