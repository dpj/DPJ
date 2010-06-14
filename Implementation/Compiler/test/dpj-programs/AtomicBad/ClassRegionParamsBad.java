class C<region atomic R> {
    region r; // Not atomic
    C<r> x; // Not allowed!
}