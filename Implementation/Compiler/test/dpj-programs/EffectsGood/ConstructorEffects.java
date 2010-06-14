class ConstructorEffects {
    int x in Root;
    ConstructorEffects(int y) pure {
	// Effects on 'this' are masked inside the constructor
	x = y;
    }
}