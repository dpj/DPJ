/* Type region parameter declared in method type parameter */

class C {
    <type T<region R>>void m() writes R {}
}