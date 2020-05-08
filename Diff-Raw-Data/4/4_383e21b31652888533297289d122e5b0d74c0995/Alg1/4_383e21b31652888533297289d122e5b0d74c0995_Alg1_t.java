 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 
 //Source of IO reading code: stackOverflow.com
 
 public class Alg1 {
 
 	/*
 	 * prints out the revisions that have some remote relevance to the queried files
 	 * - displays: revision, rating (based on algorithm #1) and the case it is under
 	 */
 	public static void printIt(Process exec, String[] arg) throws IOException {
 		BufferedReader  stdInput=  new  BufferedReader(new
 	              InputStreamReader(exec.getInputStream()));
 
 	        System.out.println(" Revision \t\t Date \t\t\t Rating \t\t\t\t Case");
 			System.out.println("|---------|-------------------------------|-------------------------------|--------------------------------------------------|");
 	        String s;
 	        char c = ' ';
 			String ss = "";
 			String date = "Never";
 			int totalChanged = 0;
 			int relevant = 0;
 			int irrelevant = 0;
 			Double rating = 0.0;
 			String theCase = "";
 			LinkedList<Double> ratingList = new LinkedList<Double>();
 			LinkedList<String> revisionList = new LinkedList<String>();
 			LinkedList<String> caseList = new LinkedList<String>();
 			LinkedList<String> dateList = new LinkedList<String>();
 			while  ((s=  stdInput.readLine())  !=  null)  {
 				if (s.startsWith("r")) { //indicates a new revision
 					if (ss != "") { //will only be this if it is the very first iteration
 						theCase = cases(relevant, irrelevant, totalChanged, arg.length); //analyze the current case
 						rating = calcRating(relevant, irrelevant, totalChanged, arg.length); //calculate the current rating
 						if (rating > 0) { // if relevant, store it
 							ratingList.addLast(rating);
 							revisionList.addLast(ss);
 							caseList.addLast(theCase);
 							dateList.addLast(date);	
 							String rat = rating.toString();
 							String spaces = "\t  | \t";
 							if (rat.length() <= 4){
 								spaces = "\t\t" + spaces;
 							}
 							else if (rat.length() <= 8){
 								spaces = "\t" + spaces;
 							}
 							System.out.println("| "+revisionList.getLast()+ "  | \t" +dateList.getLast() + "\t  | \t" + ratingList.getLast() + spaces + caseList.getLast()+"   |");
 							System.out.println("|---------|-------------------------------|-------------------------------|--------------------------------------------------|");
 						}
 						//reset fields
 						ss = "";
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
 					date = s.substring(s.indexOf('('), s.indexOf('(')+18);
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
				dateList.addLast(date);
				System.out.println("| "+revisionList.getLast()+ "  | \t" +dateList.getLast() + "\t  | \t" + ratingList.getLast() + spaces + caseList.getLast()+"   |");
				System.out.println("|---------|-------------------------------|-------------------------------|--------------------------------------------------|");
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
 			return "Irrelevant";
 		}
 		else if (relevant == totalChanged && relevant == nRel){
 			return "Very Relevant, contains only queried files";
 		}
 		else if (relevant == totalChanged && relevant < nRel) {
 			return "A Pure Subset of some of the queried files";
 		}
 		else if (relevant == nRel && relevant != totalChanged){
 			return "Impure Superset of the queried files found";
 		}
 		
 		else return "Subset of Relevants mixed with Irrelevants";
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
 	
 	public static String path(Process exec) throws IOException{
 		BufferedReader  stdInput=  new  BufferedReader(new
 	              InputStreamReader(exec.getInputStream()));
 	         
 	         return stdInput.readLine();
 	}
 	
 	public static void main(String args[]){
 		try {
 			Process exec;
 			exec = Runtime.getRuntime().exec("pwd");
 			String p = path(exec);
 			System.out.println(p + "\n");
 			exec = Runtime.getRuntime().exec("svn log -q -v "+p+"/log4j");
 			printIt(exec, args);
 		}
 		
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
