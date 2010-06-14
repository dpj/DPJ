// Stress test the class parameter and argument syntax
class C<type T1, T2; region R1, R2; effect E1, E2 |
	R1 # R2, R1 # Root, R2 # Root;
	effect E1 # reads Root, R1, R2 writes Root, R1, R2 effect E1, E2;
	effect E2 # reads Root, R1, R2 writes Root, R1, R2 effect E1, E2> {
    region r1, r2;
    C<T1,T2; R1,R2; reads R1:r2; reads R2:r1> x;
}
