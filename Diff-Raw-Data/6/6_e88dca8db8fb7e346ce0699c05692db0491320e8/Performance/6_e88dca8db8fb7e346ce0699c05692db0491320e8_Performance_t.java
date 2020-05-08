 package test.performance;
 
 import java.io.FileNotFoundException;
 
 import core.Solution;
 import core.Solver;
 
 /**
  * A class to run some rough performance tests to get an idea of the
  * effectiveness of the optimizations in searching.
  * 
  * @author Steve
  *
  */
 public class Performance {
 	
 	/**
 	 * @param args not used
 	 */
 	public static void main(String[] args) {
 		Solver solver;
 		String problem = "qwertyuiop";
 
 		try {
 			solver = new Solver("dictionary.txt");
 			
 			printResults(solver, problem);
 			
 		} catch (FileNotFoundException e) {
 			System.out.println("Can't find dictionary file: " + e.getMessage());
 			System.exit(-1);
 		}
 	}

 	
 	/**
 	 * Return a string that shows elapsed time in a human readable format with
 	 * correct English grammar.
 	 * 
 	 * @param elapsedTime in milliseconds
 	 * @return elapsed time as a string
 	 */
 	static public String time(long elapsedTime) {
 		StringBuilder time = new StringBuilder();
 		long milliseconds = elapsedTime % 1000;
 		long seconds = (elapsedTime / 1000) % 60;
 		long minutes = (elapsedTime / 60000) % 60;
		long hours = (elapsedTime / 3600000);
 		boolean hasHigherOrder = false;
 		
 		if (hours != 0) {
 			hasHigherOrder = true;
 			if (hours == 1) {
 				time.append("1 hour ");
 			} else {
 				time.append(hours);
 				time.append(" hours ");
 			}
 		}	
 		if (minutes != 0 || hasHigherOrder == true) {
 			hasHigherOrder = true;
 			if (minutes == 1) {
 				time.append("1 munute ");
 			} else  {
 				time.append(minutes);
 				time.append(" minutes ");
 			}
 		}
 		if (seconds != 0 || hasHigherOrder == true) {
 			hasHigherOrder = true;
 			if (seconds == 1) {
 				time.append("1 second ");
 			} else {
 				time.append(seconds);
 				time.append(" seconds ");
 			}
 		}
 		if (hasHigherOrder == true) {
 			time.append("and ");
 		}
 		if (milliseconds == 1) {
 			time.append("1 millisecond");
 		} else {
 			time.append(milliseconds);
 			time.append(" milliseconds");
 		}
 		
 		return time.toString();
 	}

 	
 	/**
 	 * A method to keep all output together. It solves the problem and prints
 	 * various statistical information out.
 	 * 
 	 * @param solver the engine to solve the problem
 	 * @param problem the problem to solve
 	 */
 	static public void printResults(Solver solver, String problem) {
 		Solution solution;
 		long startTime;
 		long elapsedTime;
 		
 		System.out.format("Start solving '%s'%n", problem);
 		startTime = System.currentTimeMillis();
 		solution = solver.getSolution(problem);
 		elapsedTime = System.currentTimeMillis() - startTime;
 		System.out.format("Finished.%n");
 		System.out.format("Words found: %d%n", solution.getWordSetSize());
 		System.out.format("Time taken: %s%n", Performance.time(elapsedTime));
 	}
 
 }
