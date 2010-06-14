// This test exercises the compiler effect summarization by use of an "if" statement.
class C {
    region r1, r2;
    int x in r1;
    int y in r2;
    
    // The compiler should complain about the missing "reads Root : r1" effect
    // arising from the expression "x == 0".
    void m() writes Root : r2 {
	if (x == 0)
	    y = 1;
	else
	    y = 0;
    }
}