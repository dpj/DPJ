/**
 * DPJ implementation of list ranking
 * Robert L. Bocchino Jr.
 * June 2008
 */

import DPJRuntime.*;

/**
 * Class for a list node, parameterized by a parent region
 */
class ListNode<region Parent> {

    /**
     * Region for storing rank information
     */
    region Rank;

    /**
     * Region for storing rank information we read out of the rank
     * neighbor.
     */
    region NbrRank;

    /**
     * The next node in the list containing this node.  Initially,
     * next=this (a self loop), indicating this is a terminal node of
     * the list.
     */
    public ListNode<*> next in Parent : Rank = this;

    /**
     * The neighbor node for the pointer-jumping algorithm: initially,
     * it is equal to this; then it is modified by the algorithm.
     */
    public ListNode<*> rankNbr in Parent : Rank;

    /**
     * The rank information for this node.
     */
    public int rank in Parent : Rank;

    /**
     * A place to store the value of rankNbr.rank.
     */
    public int savedRank in Parent : NbrRank;

    /**
     * A place to store the value of rankNbr.rankNbr.
     */
    public ListNode<*> savedRankNext in Parent : NbrRank;

    /**
     * Rank initialization: rank starts out as 1 unless this is a
     * self-loop (terminal node), in which case it's 0.  rankNbr
     * initially points to this.next.
     */
    void initRank() writes Parent : * {
	rank = (next == this) ? 0 : 1;
	rankNbr = next;
    }

    /**
     * Save rankNbr.rank to this.rank and rankNbr.rankNbr to
     * this.rankNbr.
     */
    void updateNbrRank() 
	reads * : Rank writes Parent : NbrRank {
	savedRank = rankNbr.rank;
	savedRankNext = rankNbr.rankNbr;
    }

    /**
     * Use the saved rank information to Update the rank of this node.
     */
    void updateRank() writes Parent : * {
	if (rankNbr != savedRankNext) {
	    rank += savedRank;
	    rankNbr = savedRankNext;
	}
    }
}

/**
 * A List composed of ListNodes.
 */
class List {

    /**
     * An array storing the nodes.  Cell nodes[i] lies in region
     * Root:[i] and has type ListNode<Root:[i]>.
     */
    ListNode<[i]>[]<[i]>#i nodes;

    /**
     * Construct a new list, of specified size.
     */
    List(int size) {
	nodes = new ListNode<[i]>[size]<[i]>#i;
	foreach (int i in 0, size) {
	    nodes[i] = new ListNode<Root:[i]>();
	}
    }

    /**
     * Rank the list.
     */
    public void rank() {
	// Initialize the nodes for ranking
	foreach (int i in 0, nodes.length) {
	    nodes[i].initRank();
	}
	// Repeat log2(nodes.length) times
	int i = Utils.log2(nodes.length);
	while (i-- > 0) {
	    // Get the rank information from the rank neighbor
	    foreach (int j in 0, nodes.length) {
		nodes[j].updateNbrRank();
	    }
	    // Update the rank information
	    foreach (int j in 0, nodes.length) {
		nodes[j].updateRank();
	    }
	}
    }
}

/**
 * A test class for list ranking.
 */
class ListRanking extends Harness {

    /**
     * The list to rank
     */
    private List list;

    /**
     *
     */
    private int[] idxs;

    /**
     * Utility method:  swap two values in array A.
     */
    private static void swap(int[] A, int i, int j) {
	int tmp = A[j];
	A[j] = A[i];
	A[i] = tmp;
    }

    /**
     * Utility method: create an array containing a random permutation
     * of the values 0..size-1.
     */
    public static int[] permutation(int size) {
	java.util.Random rand = new java.util.Random(10);
	int[] result = new int[size];
	for (int i = 0; i < size; ++i) {
	    result[i] = i;
	}
	for (int i = 0; i < size; ++i) {
	    int j = (int) (rand.nextFloat() * size);
	    int k = (int) (rand.nextFloat() * size);
	    swap(result, j, k);
	}
	return result;
    }

    public ListRanking(String[] args) {
	super("ListRanking", args, 2, 3);
    }

    @Override
    public void initialize() {
	list = new List(size);
	idxs = permutation(size);
	for (int i = 0; i < size-1; ++i) {
	    list.nodes[idxs[i]].next = list.nodes[idxs[i+1]];
	}
	list.nodes[idxs[size-1]].next = list.nodes[idxs[size-1]];
    }

    @Override
    public void runTest() {
	for (int i = 0; i < size; ++i) {
	    assert (list.nodes[idxs[i]].rank == size-i-1);
	}
    }

    @Override
    public void runWork() {
	list.rank();
    }

    @Override
    public void usage() {
	System.err.println("Usage:  java " + progName + ".java [mode] [size] [iter_size]");
	System.err.println("mode = TEST, IDEAL, TASK_GRAPH");
	System.err.println("size = problem size (int)");
	System.err.println("iter_size = foreach iterations per task (int)");
    }

    public static void main(String[] args) {
	ListRanking lr = new ListRanking(args);
	lr.run();
    }
}
