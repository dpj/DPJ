class ConstructorEffects {
    static int x in Root;
    ConstructorEffects(int y) pure {
	// Not masked, because x is not part of 'this'
	x = y;
    }
}