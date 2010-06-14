// Mismatched RPLs in array assignment

class C {
    region r1, r2;
    C[]<r1> A = new C[10]<r2>; // Error
}