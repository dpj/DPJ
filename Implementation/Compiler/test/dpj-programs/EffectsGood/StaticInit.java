abstract class StaticInit {
    region r;
    static int y in r;
    // Initializer writes r
    static {
	init();
    }
    public static void init() writes r { y = 5; }
    // OK, don't need to report static initializer effect
    public StaticInit() pure {}
}