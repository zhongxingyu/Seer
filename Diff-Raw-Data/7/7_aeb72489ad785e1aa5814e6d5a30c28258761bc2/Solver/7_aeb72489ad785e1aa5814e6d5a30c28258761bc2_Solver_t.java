 package algo1.puzzle;
 
 import algs4.MinPQ;
 import algs4.Queue;
 import algs4.Stack;
 import stdlib.In;
 import stdlib.StdOut;
 
 import java.util.Comparator;
 
 public class Solver {
   private SearchNode currentSearchNode;
 
   private static class BoardSolver {
     private SearchNode currentNode;
     MinPQ<SearchNode> pq;
 
     public BoardSolver(Board initial) {
       pq = new MinPQ<SearchNode>(1, new SearchNodeCompare());
       currentNode = new SearchNode(initial, null, 0);
       pq.insert(currentNode);
     }
 
     public SearchNode currentNode() {
       return currentNode;
     }
 
     public boolean solved() {
       return currentNode.board.isGoal();
     }
 
     public int queueSize() {
       return pq.size();
     }
 
     public void step() {
       if (!solved() && !pq.isEmpty()) {
         SearchNode node = pq.delMin();
         if (!node.board.isGoal()) {
           for (Board b : node.board.neighbors()) {
            if (currentNode.prev == null || !b.equals(node.prev.board))
               pq.insert(new SearchNode(b, node, node.moves + 1));
           }
         }
         currentNode = node;
       }
     }
   }
 
   // find a solution to the initial board (using the A* algorithm)
   public Solver(Board initial) {
     BoardSolver solver = new BoardSolver(initial);
     BoardSolver twinSolver = new BoardSolver(initial.twin());
 
     while (!solver.solved() && !twinSolver.solved()) {
       solver.step();
       System.out.println("current node \n" + solver.currentNode().board +
           "with moves " + solver.currentNode().moves +
           " hamming " + solver.currentNode().board.hamming() +
           " with " + solver.queueSize() + " boards in pq\n");
       if (solver.solved()) {
         currentSearchNode = solver.currentNode();
         break;
       }
       twinSolver.step();
       if (twinSolver.solved()) {
         currentSearchNode = null;
         break;
       }
     }
   }
 
   // is the initial board solvable?
   public boolean isSolvable()  {return currentSearchNode != null;}
   // min number of moves to solve initial board; -1 if no solution
   public int moves() {return isSolvable() ? currentSearchNode.moves : -1;}
 
   // sequence of boards in a shortest solution; null if no solution
   public Iterable<Board> solution() {
     Stack<Board> path = new Stack<Board>();
     SearchNode curr = currentSearchNode;
 
     while (curr != null) {
       path.push(curr.board);
       curr = curr.prev;
     }
     return path;
   }
 
   private static class SearchNodeCompare implements Comparator<SearchNode> {
     @Override public int compare(SearchNode first, SearchNode second) {
       int firstCost = first.board.manhattan() + first.moves;
       int secondCost = second.board.manhattan() + second.moves;
      if (firstCost > secondCost) return 1;
      else if (firstCost < secondCost) return -1;
      else return 0;
     }
   }
 
   private static class SearchNode {
     public Board board;
     public SearchNode prev;
     public int moves;
 
     SearchNode(Board board, SearchNode prev, int movesToHere) {
       this.board = board;
       this.prev = prev;
       this.moves = movesToHere;
     }
 
     @Override
     public String toString() {
       return board + "@" + moves;
     }
   }
 
   public static void main(String[] args) {
     // create initial board from file
     In in = new In(args[0]);
     int N = in.readInt();
     int[][] blocks = new int[N][N];
     for (int i = 0; i < N; i++)
       for (int j = 0; j < N; j++)
         blocks[i][j] = in.readInt();
     Board initial = new Board(blocks);
 
     // solve the puzzle
     Solver solver = new Solver(initial);
 
     // print solution to standard output
     if (!solver.isSolvable())
       StdOut.println("No solution possible");
     else {
       StdOut.println("Minimum number of moves = " + solver.moves());
       for (Board board : solver.solution())
         StdOut.println(board);
     }
   }
 }
