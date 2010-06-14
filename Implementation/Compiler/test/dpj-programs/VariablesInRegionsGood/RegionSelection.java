// Test region selection through packages and classes

package A;

class B {
    class C {
        region r;
        int x in A.B.C.r;
    }
}
