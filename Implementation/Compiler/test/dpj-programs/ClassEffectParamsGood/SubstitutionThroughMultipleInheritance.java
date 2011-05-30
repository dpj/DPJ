class Super<effect F> {
	public void m() effect F {}	
}

class Child1<effect E> extends Super<effect E> {
	//public void m() effect E {}
}

class Child2 extends Child1<writes Root> {
	public void m() reads Root {
	    Super<writes Root> s = this;
	}
}
