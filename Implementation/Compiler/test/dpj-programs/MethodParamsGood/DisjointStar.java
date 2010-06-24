class DisjointStar {
    region X, Y;
    void caller() {
	this.<region X,Y>callee();
    }
    
    public <region A, B | A:* # B> void callee() {}
}
