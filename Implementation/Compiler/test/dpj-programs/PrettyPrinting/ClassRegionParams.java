/**
 * Basic class region parameter
 */
class C1<region P> {
    region r;
    C1<r> field;
    void m1() {
	field = new C1<r>();
	C1<P> local = new C1<P>();
    }
}

/**
 * Class with region parameter and type parameter
 */
class C2<type T, region P> {
    region r;
    C2<T,r> field;
    void m1() {
        field = new C2<T,r>();
        C2<T,P> local = new C2<T,P>();
    }
}

/**
 * Two class region parameters, one declared under an RPL
 */
class C3<region P1, P2> {
    region r1;
}

/**
 * Two class region parameters, one declared under an RPL and 
 * both declared disjoint
 */
class C4<region P1, P2 under C4.r1 | P1 # P2> {
    region r1;
}

/**
 * One class region parameter shared with the superclass
 */
class C5<region P,Q> extends C1<Q> {
    region s;
    C5<r,s> anotherField;
}