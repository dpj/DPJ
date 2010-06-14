// Trying to assign with incompatible RPL bindings

class C<region R1> {
    region r1, r2;
    C<r1> x = new C<r2>();
}
