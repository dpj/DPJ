abstract class ConstraintsNotSatisfied {
    abstract <effect E | effect E # writes Root>void method1();
    void method2() {
	// Constraint on variable E in mehthod 1 is not satisfied
	this.<writes Root>method1();
    }
    
}