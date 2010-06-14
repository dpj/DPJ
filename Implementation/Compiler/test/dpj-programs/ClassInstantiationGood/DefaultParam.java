// Test use of Root as default class param

class C<region R1, R2> {
    // Where no params specified
    C<Root,Root> a = new C(); 
    C b = new C<Root,Root>();
    // Where too few params specified
    C<R1,Root> c = new C<R1>();
    C<R1> d = new C<R1,Root>();
}	