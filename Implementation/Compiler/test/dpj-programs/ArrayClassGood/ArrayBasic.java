/**                                                                                                                         
 * Basic test of generic array
 */

arrayclass Array<T> { T; }

class Data {}

class ArrayBasic {
    Array<Data> m() {
        Array<Data> a = new Array<Data>(10);
        for (int i = 0; i < a.length; ++i)
            a[i] = new Data();
        return a;
    }
}
