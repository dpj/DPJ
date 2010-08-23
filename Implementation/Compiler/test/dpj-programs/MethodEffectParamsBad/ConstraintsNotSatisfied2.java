/**
 * More complicated example than ConstraintsNotSatisfied.
 * This one also uses class region params.
 */
class ConstraintsNotSatisfied<type T<region TR>, region Cont> {
    static class Data<region R>{}
    public <effect E | effect E # writes Cont effect E>
        void doWork() {}
    public static void main(String[] args) {
        ConstraintsNotSatisfied<Data<Root>,Root> cns =
            new ConstraintsNotSatisfied<Data<Root>,Root>();
        // E bound to reads Root, Cont bound to Root
        // ==> E interferes with writes Cont
        cns.<reads Root>doWork();
    }
}
