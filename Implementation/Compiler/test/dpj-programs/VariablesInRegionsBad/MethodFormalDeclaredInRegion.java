class C {
    // This is wrong: x goes in an implicit stack region, not Root
    void m(int x in Root) {}
}