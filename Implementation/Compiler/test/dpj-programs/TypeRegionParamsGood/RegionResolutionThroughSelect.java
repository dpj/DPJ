class A<type T<region R>> {
    int x in R;
    void m() writes Root {
	new A<B<Root>>().x = 5;
    }
}

class B<region R> {}