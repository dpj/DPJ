abstract class BadStaticInit {
    region r;
    static int y in r;
    // Initializer writes r
    {
	init();
    }
    public static void init() writes r { y = 5; }
    // Effect must be reported in constructor
    public BadStaticInit() pure {}
}