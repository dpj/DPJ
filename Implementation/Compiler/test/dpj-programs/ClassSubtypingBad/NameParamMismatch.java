// Trying to assign with incompatible RPL bindings

class C<region P> {
    region r;
    C<r> x = new C<P>();
}
