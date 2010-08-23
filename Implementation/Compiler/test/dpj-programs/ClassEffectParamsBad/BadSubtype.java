class BadSubtype<effect E> {
    // Should be caught
    BadSubtype<pure> x = new BadSubtype<writes Root>();
}