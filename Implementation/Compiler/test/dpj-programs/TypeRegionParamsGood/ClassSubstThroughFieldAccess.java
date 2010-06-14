/** Test substitution of type region param through field access */

class A<type T<region R1>> {
    region r;
    T<A.r> t;
}

class B<region R2> {
    region r;
    // B.r gets changed to A.r by the type of A#t
    A<B<B.r>> a;
    B<A.r> c = a.t;
}

