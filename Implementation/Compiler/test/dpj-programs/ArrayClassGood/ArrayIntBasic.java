/**                                                                                                                         
 * Basic test of int array
 */

arrayclass ArrayInt { int; }

class ArrayIntBasic {
    ArrayInt m() {
        ArrayInt a = new ArrayInt(10);
        for (int i = 0; i < a.length; ++i)
            a[i] = i;
        return a;
    }
}
