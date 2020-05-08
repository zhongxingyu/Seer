 package framework;
 
 import java.util.*;
 import java.util.concurrent.*;
 
 /*
  
  * Generic Concurrent puzzle solver. 
  * Field puzzle holds the concrete puzzle.
  * Field seen contains the positions seen so far.
 
 */
 
 /*
  * 
  * Essentially, the solver performs a depth-first search over the search space.
  * Method solve() starts the search by initializing the list of nodes with a 
  * node that contains the initial position, and a null previous node.
  * Method search(Node) examines the current node. If seen contains the 
  * position of the node, search returns a null list (i.e., the search 
  * stops at this branch). Otherwise, the search examines if the puzzle 
  * has reached the goal position, in which case it returns the solution in 
  * the form of a list of positions. If the puzzle has not reached the goal 
  * position yet, the search continues trying out each legal position that 
  * is a descendant of the current one. 
  * 
 */
 
 public class ConcurrentPuzzleSolver {
 	
 	class MyThread implements Callable {
 		Node n;
 		int level;
 		public MyThread(Node node, int deep){
 			n = node;
 		 	level = deep;
 		}
 		public LinkedList call() throws CancellationException{	
 			
 			LinkedList result = search(n, level);
 			if (result == null) {
 				throw new CancellationException("no result found");
 			}
 			return result;
 			
 			
 		}
 		
 
 		
 		
 	}
 	//put a lock on seen
 	
 	private final Puzzle puzzle;
 	private final ConcurrentHashMap<Puzzle, Node> seen = new ConcurrentHashMap<Puzzle, Node>();
 	
 	
 	public ConcurrentPuzzleSolver(Puzzle puzzle) {
 		this.puzzle = puzzle;
 	}
 	
 	public LinkedList solve() {
 		puzzle.initialPosition();
 		return search(new Node(puzzle, null), 1);
 	}
 	
 	/*
 	 * Purpose:  To see if the current state is solved and make threads to run
 	 * the same test on its children
 	 * Arguments:  Node, int
 	 * Outputs: LinkedList
 	 */
 	public LinkedList search(Node node, int level) {
 		ExecutorService e;
		if (level%5==0)
 			e = Executors.newFixedThreadPool(2);
		else
			e = Executors.newFixedThreadPool(1);
 		Set<Callable<LinkedList>> set = new HashSet<Callable<LinkedList>>();
 		if (seen.putIfAbsent(node.pos, node) == null) {
 			if (node.pos.isGoal()) { 
 				return node.asPositionList();
 			}
 			//Run the search function on the Children
 			LinkedList children = node.pos.legalMoves(node);
 			for (Object o : children) { 
 				
 				Puzzle puzzle = (Puzzle) o;
 				Node child = new Node(puzzle, node);
 				
 				Callable<LinkedList> task = new MyThread(child, level+1);
 				set.add(task);
 						
 			}
 			LinkedList result = null;
 			try {
 				result = e.invokeAny(set);
 			} catch (ExecutionException e1){
				e.shutdownNow();
 				return null;
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 			}
			e.shutdownNow();
 			return result;
 			
 		}
 		
 		return null;
 	}
 	
 }
