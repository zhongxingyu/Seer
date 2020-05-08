 /*
  * MAIN class for Watson submission. Takes CSV file and passes to the three
  * implemented ML algorithms for processing. Any candidates that are chosen by more
  * than 1 algorithm are marked as likely answers and shown.
  */
 package kmad;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.lang.Thread;
 
 /**
  * 
  * @author Ian and Joseph, modifications made by Eric.
  */
 public class KMAD {
 
 	/**
 	 * @param args
 	 *            the command line arguments
 	 */
 	public static void main(String[] args) throws InterruptedException {
 		// TODO code application logic here
 		File inFile = new File("./tgmctrain.csv");
 		// File inFile = new File("TGMC training-sample.csv");
 //		Scanner in = null;
 		File inFile2 = new File("min-max_values.txt");
 		Scanner in2 = null;
 		ArrayList<Candidate> candidates = new ArrayList<Candidate>();
 		
 		
 /**		//TODO: Changes to pull file input out of wrapper here! - Joseph Doherty
 		try {
 			in = new Scanner(inFile);
 		} catch (FileNotFoundException e) {
 			System.out.println("Input File not found");
 			System.exit(1); // can't do anything, exit.
 		}
 
 **/		String line;
 /**
 		int u = 0;
 		while (in.hasNext()) {
 			// TODO
 			u++;
 			if (u == 100) { // get rid of for real
 				break;
 			}
 			line = in.nextLine();
 			Candidate c = new Candidate(line, true);
 			candidates.add(c);
 		}**/
 
 		try {
 			in2 = new Scanner(inFile2);
 		} catch (FileNotFoundException e) {
 			System.out.println("Input File for deltas not found");
 			System.exit(1); // can't do anything, exit.
 		}
 
 
 
 
 		double vals[] = new double[638];
 		double delta[] = new double[319];
 		int i = 0;
 		Scanner in3;
 		while (in2.hasNextLine()) {
 			line = in2.nextLine();
 			in3 = new Scanner(line);
 			in3.useDelimiter(",");
 			while (in3.hasNext()) {
 				vals[i] = Double.parseDouble(in3.next());
 				i++;
 			}
 			in3.close();
 		}
 		in2.close();
 		
 		for (i = 0; i < 319; i++) {
 			delta[i] = Math.abs((vals[i] - vals[i + 230]));
 			// System.out.println(delta[i]);
 		}
 
 		//Call the three approaches.
 		// TODO: Add calling method for ID3
 		Genetic genetic = new Genetic(candidates, delta, inFile);
 		genetic.run();
 			//For now, precision is passed in directly as 3 for J48.
 		J48 tree = new J48(inFile, 3, false, false, candidates);
 		J48.process();
 		
 
 		//If your approach directly returns the Cand ArrayList, assign it here.
 		ArrayList<SpecTuple> filteredCandIDThree = new ArrayList<SpecTuple>();
 		ArrayList<SpecTuple> filteredCandGenetic = new ArrayList<SpecTuple>();
 		ArrayList<SpecTuple> filteredCandJ48 = new ArrayList<SpecTuple>();
 
 		/**
 		 * //Start children for different approaches Thread IDThree = new
 		 * Thread((new IDThree(filteredCandIDThree))); IDThree.start(); Thread
 		 * Genetic = new Thread((new Genetic(filteredCandGenetic, delta)));
 		 * Genetic.start();
 		 * 
 		 * //Wait for children IDThree.join(); Genetic.join();
 		 **/
 
 		// Average and find best scores
 		ArrayList<SpecTuple> averagedCand = analyze(filteredCandGenetic,filteredCandIDThree, filteredCandJ48);
 		System.out.println(averagedCand.toString());
 
 	}
 
 	/**
 	 * Checks between the given ArrayLists for crossovers. If a candidate exists
 	 * in more than 1 ArrayList, it is considered a viable answer, given an
 	 * averaged score, and added to the new ArrayList.
 	 * 
 	 * @param Genetic
 	 * @param IDThree
 	 * @param Other
 	 * @return finalCands
 	 */
 	private static ArrayList<SpecTuple> analyze(ArrayList<SpecTuple> Genetic,ArrayList<SpecTuple> IDThree, ArrayList<SpecTuple> J48) {
 		ArrayList<SpecTuple> finalCands = new ArrayList<SpecTuple>();
 		
 		//If the sets entered are empty, alert user
		if(Genetic.isEmpty() || IDThree.isEmpty() || J48.isEmpty())
 
 		finalCands = crossOver(finalCands, Genetic, IDThree);
 		finalCands = crossOver(finalCands, IDThree, J48);
 		finalCands = crossOver(finalCands, J48, Genetic);
 
 		return finalCands;
 	}
 
 	// Helper function for analyze, checks if a candidate in one ArrayList
 	// exists in the other given ArrayList
 	private static ArrayList<SpecTuple> crossOver(ArrayList<SpecTuple> candidates, ArrayList<SpecTuple> arrayX,ArrayList<SpecTuple> arrayY) {
 		SpecTuple current;
 
 		for (int i = 0; i < arrayX.size(); i++) {
 			for (int k = 0; k < arrayY.size(); k++) {
 				current = arrayX.get(i);
 				if (current.sameID(arrayY.get(k))
 						&& !contains(candidates, current)) {
 					current.average(arrayY.get(k));
 					candidates.add(current);
 				}
 			}
 		}
 
 		return candidates;
 	}
 
 	// Helper function for analyze, checks if the given ArrayList contains the
 	// candidate
 	private static boolean contains(ArrayList<SpecTuple> candidates,SpecTuple cand) {
 		for (int i = 0; i < candidates.size(); i++) {
 			if (candidates.get(i).ID == cand.ID) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 }
