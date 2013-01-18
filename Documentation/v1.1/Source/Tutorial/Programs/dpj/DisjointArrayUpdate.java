class DisjointArrayUpdate {
    static class Data<region R> {
        int x in R;
        static arrayclass Array {
            Data<[index]> in [index];
        }
    }
    Data.Array arr;
    void initialize() {
        arr = new Data.Array(10);
        foreach (int i in 0, 10)
            arr[i] = new Data<[i]>();
    }
    void compute() {
        foreach (int i in 0, 10)
            ++arr[i].x;
    }
    public static void main(String[] args) {
        DisjointArrayUpdate example = new DisjointArrayUpdate();
        example.initialize();
        example.compute();
        for (Data<[?]> data : example.arr)
            System.out.print(data.x + " ");
        System.out.println();
    }
}
