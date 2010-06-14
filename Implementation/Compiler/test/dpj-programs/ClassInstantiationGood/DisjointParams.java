/** Test of disjointness constraints satisfied
 */
class C<region R1, R2 | R1 # R2> {
    region r1, r2;
    C<r1,r2> x; // OK because r1 =/= r2
    C<R1,R2> y; // OK because R1 # R2 in declaration
}