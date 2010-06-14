/**
 * The effect for C.m1() should be "writes r1:*" not "writes this".
 *  
 * @author Mohsen Vakilian
 */
class C<region R1> {
  region r1;

  void m1() writes this {
    new C<r1>().m2();
  }

  void m2() writes this {
  }
}
