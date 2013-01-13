abstract class BadStaticFieldInit {
    region r;
    static int y in r;
    // Initializer writes r
    public static final int x = init();
    public static int init() writes r { y = 5; }
    // Effect must be reported in constructor
    public BadStaticFieldInit() pure {}
}