class EffectConstraintNotSatisfied<effect E | effect E # reads Root> {
    // Compiler should warn that constraint is not satisfied
    EffectConstraintNotSatisfied<writes Root> x;
}
