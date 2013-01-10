/**
 * This example, due to Mohsen Vakilian, motivated a bug fix in the
 * handling of z regions.
 */
abstract class ZRegion2<region R> {
    abstract void m1(final ZRegion2<R> c) writes c;
    void m2()
        writes this
    {
	// 'this' was not substituted for c here
        m1(this);
    }
}
