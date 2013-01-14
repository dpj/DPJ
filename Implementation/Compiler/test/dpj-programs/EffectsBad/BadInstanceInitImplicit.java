class BadInstanceInitImplicit {
    region r;
    // Initializer writes r
    {
	init();
    }
    public abstract void init() writes r;
    // Implicit constructor effect is 'pure'
}