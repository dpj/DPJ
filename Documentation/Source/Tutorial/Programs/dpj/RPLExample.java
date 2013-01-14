class RPLExample {
    region A, B, C;
    int x in A:B;
    int y in A:C;
    void method(int x, int y) writes A:* {
        this.x = x;
        this.y = y;
    }
}

