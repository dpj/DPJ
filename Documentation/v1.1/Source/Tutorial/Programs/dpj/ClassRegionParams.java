class Data<region R> {
    int x in R;
}
class DataPair {
    region First, Second;
    final Data<First> first;
    final Data<Second> second;
    // Constructor initialization effects don't have to be reported
    // See Reference Manual s. 2.3.2
    DataPair(Data<First> first, Data<Second> second) 
        pure 
    {
        this.first = first;
        this.second = second;
    }
    void updateBoth(int firstX, int secondX) {
        cobegin {
            // Effect is 'writes First'
            first.x = firstX;
            // Effect is 'writes Second'
            second.x = secondX;
        }
    }
}
