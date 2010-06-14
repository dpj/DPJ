/**
 * The compiler should complain about the effect "writes c2" for c2 is not a final variable.
 * @author Mohsen Vakilian
 *
 */

class C1 {
  C1 x;
  void m1() writes x {}
}
