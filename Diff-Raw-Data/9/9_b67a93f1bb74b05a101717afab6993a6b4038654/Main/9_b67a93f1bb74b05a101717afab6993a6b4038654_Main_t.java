 package main;
 
 import search.Astar;
 import state.State;
 import state.Utils;
 
 public class Main {
 	
 	static int SCRAMBLE_TIMES = 100;
 	static public int NUM_STACKS = 5;
 	static int NUM_BLOCKS = 7;
 	
 	static int[] SCRAMBLES = {5, 10, 20, 50, 100, 1000};
 	
 	
 	public static void main(String[] args) {
		State goal = Utils.CreateGoalState(NUM_STACKS, NUM_BLOCKS);
		State random = Utils.Randomize(goal, 15);
		//random.Print();
		Astar astar = new Astar();
		State solution = astar.FindSolution(random);
		Utils.PrintSolutionPath(solution);
		// runTest();
 	}
 
 	public static void runTest() {
 		State goal = Utils.CreateGoalState(NUM_STACKS, NUM_BLOCKS);
 		double goalTests = 0;
 		double maxQueueSize = 0;
 		double iteration = 0;
 		double depth = 0;
 		int counter = 0;
 		
 		for (int s = 0; s < 1; s++) {
 			goalTests = 0;
 			maxQueueSize = 0;
 			iteration = 0;
 			depth = 0;
 			counter = 0;
 			for (int i = 0; i < 1; i++) {
 				State random = Utils.Randomize(goal, 15);
 				//random.Print();
 				Astar astar = new Astar();
 				State solution = astar.FindSolution(random);
 
 				if(solution != null) {
 					goalTests += astar.goalTests;
 					maxQueueSize += astar.maxQueueSize;
 					iteration += astar.iteration;
 					depth += astar.depth;
 					++counter;
 				}
 				Utils.PrintSolutionPath(solution);
 			}
 			goalTests/=counter;
 			maxQueueSize/=counter;
 			iteration/=counter;
 			depth/=counter;
 			counter/=counter;
 			//System.out.println("Scrambles=" + SCRAMBLES[s]);
 			//System.out.println("depth=" + depth + "\t Goal Tests=" + goalTests + "\t Max Queue Size=" + maxQueueSize);
 
 		}
 		
 		
 	}
 }
