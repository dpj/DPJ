// Test bad assignment (through a return) when default region binding
// is used.

class C<region R> {
    region r;
    C m() {
        return new C<r>();
    }
}
