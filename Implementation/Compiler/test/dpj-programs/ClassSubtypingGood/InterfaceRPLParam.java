// Tests substitution of RPL params through an interface

interface A<region RA> {

}

class B<region RB> implements A<RB> {

}

class C<region RC> {
    A<RC> m() { return new B<RC>(); }
}

