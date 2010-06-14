/** Test substitution of type region param through field access */

class A<type T1<region R1>> {
    region r;
    T1<A.r> t;
}

class B<type T2<region R2>> {
    region r;
    // B.r gets changed to A.r by the type of A#t
    A<T2<B.r>> a;
    T2<A.r> c = a.t;
}
