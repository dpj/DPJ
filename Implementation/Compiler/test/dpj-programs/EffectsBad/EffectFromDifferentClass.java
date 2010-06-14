class C1 {
  region r1;
  int x in r1;
  
  void m1() writes r1 {
    x = 0;
  }
}

class C2 {
  void m2() pure {
    new C1().m1();
  }
}
