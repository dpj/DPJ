class Point {
    region X, Y;
    double x in X;
    double y in Y;
    void setX(double x) writes X { this.x = x; }
    void setY(double y) writes Y { this.y = y; }
    void setXAndY(double x, double y) 
        writes X, Y 
    {
        cobegin {
            this.setX(x);
            this.setY(y);
        }
    }
}
