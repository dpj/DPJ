// Test of disjointness constraints for class region params

class C<region P1,P2 | P1#P2> {
    region r;
    C<r,r> x; // Should warn that disjointness is violated
}