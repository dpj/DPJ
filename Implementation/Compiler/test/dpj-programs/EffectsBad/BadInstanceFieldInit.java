abstract class BadInstanceFieldInit {
    region r;
    // Initializer writes r
    public final int x = init();
    public abstract int init() writes r;
    // Effect must be reported in constructor
    public BadInstanceFieldInit() pure {}
}