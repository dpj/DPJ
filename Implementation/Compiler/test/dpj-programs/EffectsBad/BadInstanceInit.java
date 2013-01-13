abstract class BadInstanceInit {
    region r;
    // Initializer writes r
    {
	init();
    }
    public abstract void init() writes r;
    // Effect must be reported in constructor
    public BadInstanceInit() pure {}
}