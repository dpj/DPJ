abstract class StaticFieldInit {
    region r;
    static int y in r;
    // Initializer writes r
    public static final int x = init();
    public static int init() writes r { y = 5; return y; }
    // Effect of static initializer need not be reported in constructor
    public StaticFieldInit() pure {}
}