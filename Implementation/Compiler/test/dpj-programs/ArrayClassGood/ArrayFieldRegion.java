/**                                                                                                                         
 * Test of field region specifier for array class
 */

arrayclass ArrayInt<region R> { int in R; }

class ArrayFieldRegion {
    region r;
    final ArrayInt<r> a = new ArrayInt<r>(10);
    void m() writes r {
        for (int i = 0; i < a.length; ++i)
            a[i] = i;
    }
}
