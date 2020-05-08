 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 
 public class Alg1 {
 
 	/*
 	 * prints out the revisions that have some remote relevance to the queried files
 	 * - displays: revision, rating (based on algorithm #1) and the case it is under
 	 */
 	public static void printIt(Process exec, String[] arg) throws IOException {
 		BufferedReader  stdInput=  new  BufferedReader(new
 	              InputStreamReader(exec.getInputStream()));
 
 	        System.out.println("Revision \t\t\t rating \t\t\t case \n");
 	        String s;
 	        char c = ' ';
 			String ss = "r";
 			int totalChanged = 0;
 			int relevant = 0;
 			int irrelevant = 0;
 			double rating = 0;
 			String theCase = "";
 			LinkedList<Double> ratingList = new LinkedList<Double>();
 			LinkedList<String> revisionList = new LinkedList<String>();
 			LinkedList<String> caseList = new LinkedList<String>();
 			while  ((s=  stdInput.readLine())  !=  null)  {
 				if (s.startsWith("r")) { //indicates a new revision
 					if (ss != "r") { //will only be this if it is the very first iteration
 						theCase = cases(relevant, irrelevant, totalChanged, arg.length); //analyze the current case
 						rating = calcRating(relevant, irrelevant, totalChanged, arg.length); //calculate the current rating
 						if (rating > 0) { // if relevant, store it
 							ratingList.addLast(rating);
 							revisionList.addLast(ss);
 							caseList.addLast(theCase);
 							System.out.println(revisionList.getLast() + "\t\t\t" + ratingList.getLast() + "\t\t\t" + caseList.getLast());
 						}
 						//reset fields
 						ss = "r";
 						relevant = 0;
 						irrelevant = 0;
 						totalChanged = 0;
 					}
 					// get the next revision number
 					int i = 1;
 					while (s.charAt(i) != c) { 
 						ss += s.charAt(i);
 						i++;
 					}					
 				}
 				// if it is not a revision, it is either a file or junk, discard junk and process the file names
 				else if (s.startsWith("   M") || s.startsWith("   D") || s.startsWith("   A")){
 					totalChanged++; // how many files were changed in the revision 
 					int i = 0;
 					String[] splitting = s.split(" "); //separate to get the file name
 					while (i < arg.length) {
						if (!arg[i].startsWith("/")) {
							arg[i] = "/" + arg[i];
						}
						if (splitting[4].contains("/logging/log4j/trunk" + arg[i]) && splitting[4].endsWith(arg[i])) { // compare to all queried files
 							i = arg.length+77;							
 						}
 						else {
 							i++;
 						}
 					}
 					if (i == arg.length+77){ //indicates presence of relevant file
 						relevant++;
 					}
 					else {
 						irrelevant++;
 					}
 				}
 	         }
 			theCase = cases(relevant, irrelevant, totalChanged, arg.length);
 			rating = calcRating(relevant, irrelevant, totalChanged, arg.length);
 			if (rating > 0) {
 				ratingList.addLast(rating);
 				revisionList.addLast(ss);
 				caseList.addLast(theCase);
 				System.out.println(revisionList.getLast() + "\t" + ratingList.getLast() + "\t" + caseList.getLast());
 			}
 	}
 	
 	/*
 	 * calculates the relevance rating for its case type based on algorithm #1
 	 */
 	private static double calcRating(int relevant, int irrelevant, int totalChanged, int nRel) {
 		if (relevant == 0) {
 			return 0;
 		}
 		else if (relevant == totalChanged && relevant == nRel){
 			return 1;
 		}
 		else if (relevant == totalChanged && relevant < nRel) {
 			return ((double)totalChanged/nRel);
 		}
 		else if (relevant == nRel && relevant != totalChanged){
 			return ((double)relevant/totalChanged);
 		}
 		
 		else return (double)relevant/(nRel + (double)irrelevant/totalChanged);
 	}
 
 	/*
 	 * Uses the same process as calcRating to discern what case the revision is in
 	 */
 	private static String cases(int relevant, int irrelevant, int totalChanged, int nRel) {
 		if (relevant == 0) {
 			return "CASE 2";
 		}
 		else if (relevant == totalChanged && relevant == nRel){
 			return "CASE 1";
 		}
 		else if (relevant == totalChanged && relevant < nRel) {
 			return "CASE 3";
 		}
 		else if (relevant == nRel && relevant != totalChanged){
 			return "CASE 5";
 		}
 		
 		else return "CASE 4";
 	}
 	
 	/*
 	 * uses the status of the directory to find how many files are in the directory
 	 */
 	public static void howMany(Process exec) throws IOException{
 		BufferedReader  stdInput=  new  BufferedReader(new
 	              InputStreamReader(exec.getInputStream()));
 	         
 	         int n = 0;
 	         @SuppressWarnings("unused")
 			String s;
 			 while  ((s = stdInput.readLine())  !=  null)  {
 		         n++;
 		     }
 			 System.out.println("Number of files: " + n + "\n");
 	}
 	
 	public static void main(String args[]){
 		try {
 			Process exec;
 			//exec = Runtime.getRuntime().exec("svn status -v /Users/justin/Documents/log4j");
 			//howMany(exec);
 			exec = Runtime.getRuntime().exec("svn log -q -v /Users/justin/Documents/log4j");
 			printIt(exec, args);
 		}
 		
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
