/**
 * This example, due to Stephen Heumann, motivated a bug fix in the
 * handling of z regions.
 */
class ZRegion1<region R> {
    ZRegion1<R> y;
    void m() {
        ZRegion1<this> z = new ZRegion1<this>();
	// z was erroneously substituted for 'this' here
        z.y = new ZRegion1<this>();
    }
}