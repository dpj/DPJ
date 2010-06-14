/** Test disjointness constraint between param and RPL
 */
class C<region R1, R2 | R1 # Root, R2 # Root> {
    region r1, r2;
    C<r1,r2> x; // OK because r1, r2 =/= Root
    C<R1,R2> y; // OK because R1, R2 # Root in declaration
}