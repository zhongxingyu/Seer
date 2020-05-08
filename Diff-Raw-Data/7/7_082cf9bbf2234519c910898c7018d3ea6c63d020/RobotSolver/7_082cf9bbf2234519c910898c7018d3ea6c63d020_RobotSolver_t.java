 package tutorial2;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import search.State;
 import search.algorithms.*;
 import search.heuristics.*;
 
 public class RobotSolver {
 	/** The default file to read input from. */
 	public static final String DEFAULT_INPUT = "problem.txt";
 	/** The default file to write the solution to. */
 	public static final String DEFAULT_OUTPUT = "solution.txt";
 
 	/** Holds the problem details */
 	private static ProblemSpec ps;
 
 	/** The list of states in the sampled state space. */
 	private static List<RobotArmState> states = new ArrayList<RobotArmState>();
 
 	/**
 	 * Randomly generates the required number of states.
 	 * 
 	 * @param numberToGenerate
 	 *            the number of states to generate.
 	 */
 	public static void generateStates(int numberToGenerate) {
 		for (int i = 0; i < numberToGenerate; i++) {
 			while (true) {
 				RobotArmState s = StateTools.createRandomState(ps.getLength1(),
 						ps.getLength2());
 				if (StateTools.isValidState(s, ps.getObstacles())) {
 					states.add(s);
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Connects together states that have a valid path between them, and an
 	 * angle delta lower than the given maximum.
 	 * 
 	 * @param maxAngleDelta
 	 *            the maximum allowable delta between neighbours.
 	 */
 	public static void connectStates(double maxAngleDelta) {
 		for (int i = 0; i < states.size(); i++) {
 			RobotArmState s1 = states.get(i);
 			for (int j = i + 1; j < states.size(); j++) {
 				RobotArmState s2 = states.get(j);
 				double totalAngleDelta = StateTools.totalAngleDelta(s1, s2);
 				if (totalAngleDelta > maxAngleDelta) {
 					continue;
 				}
 				if (!StateTools.hasDirectPath(s1, s2, ps.getObstacles())) {
 					continue;
 				}
 				s1.addSuccessor(s2, totalAngleDelta);
 				s2.addSuccessor(s1, totalAngleDelta);
 			}
 		}
 	}
 
 	/**
 	 * Writes the given solution path to a text file.
 	 * 
 	 * @param path
 	 *            the path taken from the initial state to the goal state.
 	 * @param outputPath
 	 *            the path of the output file to write to.
 	 * @throws IOException
 	 *             if there is an error writing to the output file.
 	 */
 	public static void writeOutput(List<RobotArmState> path, String outputPath)
 			throws IOException {
 		FileWriter writer = new FileWriter(outputPath);
 		for (RobotArmState s : path) {
			writer.write(String.format("%.3f %.3f%s",
 					Math.toDegrees(s.getAngle1()),
					Math.toDegrees(s.getAngle2()),
					System.getProperty("line.separator")));
 		}
 		writer.close();
 	}
 
 	/**
 	 * Runs the solver
 	 * 
 	 * @param args
 	 *            if given, the input and output files to use.
 	 */
 	public static void main(String args[]) {
 		long seed = (new Random()).nextLong();
 		System.out.println("Seed: " + seed);
 		StateTools.setSeed(seed);
 
 		String inputPath = DEFAULT_INPUT;
 		String outputPath = DEFAULT_OUTPUT;
 		if (args.length >= 1) {
 			inputPath = args[0];
 		}
 		if (args.length >= 2) {
 			outputPath = args[1];
 		}
 		ps = new ProblemSpec();
 		try {
 			ps.loadProblem(inputPath);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 		System.out.println("Init: " + ps.getInitialState());
 		System.out.println("Goal: " + ps.getGoalState());
 		System.out.println("Obs:  " + ps.getObstacles());
 		System.out.println();
 
 		states.add(ps.getInitialState());
 		states.add(ps.getGoalState());
 		System.out.println("Generating states!");
 		generateStates(10000);
 		System.out.println("Connecting graph!");
 		connectStates(10);
 
 		Heuristic heuristic;
 		heuristic = new TotalAngleDeltaHeuristic(ps.getGoalState());
 		// heuristic = new ZeroHeuristic();
 
 		AbstractSearchAlgorithm algo;
 		// algo = new DepthFirstSearch(initialState, goalState);
 		// algo = new DepthLimitedSearch(10, initialState, goalState);
 		// algo = new IterativeDeepeningSearch(initialState, goalState);
 
 		// algo = new BreadthFirstSearch(initialState, goalState);
 		algo = new AStarSearch(ps.getInitialState(), ps.getGoalState(),
 				heuristic);
 
 		System.out.println("Searching!");
 		System.out.println();
 
 		algo.verboseSearch();
 		if (algo.goalFound()) {
 			List<RobotArmState> path = new ArrayList<RobotArmState>();
 			for (State s : algo.getGoalPath()) {
 				path.add((RobotArmState) s);
 			}
 			try {
 				writeOutput(path, outputPath);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
