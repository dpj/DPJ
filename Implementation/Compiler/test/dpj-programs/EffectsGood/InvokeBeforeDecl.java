/**
 * This test illustrates that resolution of globally visible class
 * symbols is correctly happening before resolution of declared method 
 * effects.
 */

class A {
   region Ra;
   int a in Ra;

   // Symbol B.Rb is visible here, because it was entered during the
   // memberEnter phase.  Resolution of declared method effects is
   // now a separate, DPJ-specific pass that happens after memberEnter
   // but before Attr.
   void m() writes A.Ra, Root : B.Rb {
     a = 1;
     new B().m2();
   }
}

class B {
   region Rb;
   int b in Rb;

   void m2() writes B.Rb {
     b = 3;
   }
}
