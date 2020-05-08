 package Primary;
 
 import java.io.*;
 import java.util.*;
 
 
 //Source of IO reading code: stackoverflow.com
 
 public class HistoryParser {
 	
 	//bundle gets the root name of our repository address from the config.properties file
 	private ResourceBundle bundle = ResourceBundle.getBundle("config");
 	
 	// args holds the string array of passed parameters from the command line
 	private String[] args;
 	
 	private LinkedList<RevisionNode> initiallyRelevant = new LinkedList<RevisionNode>();
 	/**
 	 * Constructor: allows for access in the main code
 	 * @param arg file names to be looked for in the code
 	 */
 	public HistoryParser(String[] arg) {
 		args = arg;  // gets the user-requested files
 		int i; //loop counter
 		for (i = 0; i < args.length; i++) {
 			if (args[i].endsWith("/")) { //removes a end slash to avoid confusion in the later statistics and output
 				args[i] = args[i].substring(0, args[i].lastIndexOf('/'));
 			}
 		}
 	}
 	
 	public void nodeCycle(Process exec, int argNum) throws Exception{
 		BufferedReader  stdInput=  new  BufferedReader(new
 	              InputStreamReader(exec.getInputStream()));
 		
 		//holds the current line of input in String form
 		String s;
 		//holds the split string
 		String[] ss;
 		String userList = "";
 		//revision list
 		String rev = "thisIsNotARevision";
 		//list of revision dates
 		String date = "";
 		//counter for how many files are changed
 		int count = 0;
 		String comment = "";
 		String undesirable = bundle.getString("unwanted");
 		
 		// while there is input to process, execute this loop
 		while  ((s=  stdInput.readLine())  !=  null)  {
 			
 			if (s.startsWith("r") && s.contains("|")) {  //a line starting with a lower case r implies that we are at a new revision
 				if (count != 0) {  // check to see whether or not this is the first iteration
					if (!(undesirable.contains(" "+rev+" ") || undesirable.contains(":"+rev+" ") || undesirable.contains(":"+rev+"."))) {
 						RevisionNode thisNode = new RevisionNode(date, rev, args.length, userList, count, comment);
 						thisNode.newRelevantFile(args[argNum]);
 						sortedInsert(initiallyRelevant, thisNode);
 					}
 					comment = "";
 					count = 0;  //reset the counter
 				}
 				ss = s.split(" "); //split the string along white spaces
 				rev = ss[0].substring(1); //gets the revision number at the very beginning of s, removing the r to just get the number
 				if (ss[4].equals("|")){
 					date = ss[5]+" "+ss[6]; 
 					userList = ss[2] + ss[3];
 				}
 				else {
 					date = ss[4]+" "+ss[5];  // gets both the date and time of the revision
 					userList = ss[2];
 				}
 			}
 			//indicates that the line details a change of some sort in the file
 			else if (s.startsWith("   M") || s.startsWith("   A") || s.startsWith("   D") || s.startsWith("   R")){
 				count++; //increase the counter for files changed in a certain revision
 			}
 			else if (!s.equals("Changed paths:") && !s.contains("-------")) {
 				comment += s+" ";
 			}
 		}	
 		if (rev.equals("thisIsNotARevision")) {
 			throw new Exception("User did not enter the names properly");
 		}
		if (!(undesirable.contains(" "+rev+" ") || undesirable.contains(":"+rev+" ") || undesirable.contains(":"+rev+"."))) {
 			RevisionNode thisNode = new RevisionNode(date, rev, args.length, userList, count, comment);
 			thisNode.newRelevantFile(args[argNum]);
 			sortedInsert(initiallyRelevant, thisNode);
 		}
 	}
 	
 	/**
 	 * sortedInsert takes a RevisionNode and places it into the chosen RevisionNode linked list in such a way that
 	 * the descending order of the revision numbers is preserved
 	 * @param list the list that the new RevisionNode will be inserted into
 	 * @param node the RevisionNode to be inserted
 	 */
 	public void sortedInsert(LinkedList<RevisionNode> list, RevisionNode node){
 		
 		int i; //loop counter
 		if (list.peek() == null){ //checks if the list is empty
 			list.addFirst(node); //if the list is empty then simply place the node at the head
 		}
 		else { //otherwise the node must be compared to the node currently present in the list
 			i = 0; //set i to zero to get the full range of the list
 			while (i < list.size()){ //iterates through the whole list until the correct location is found
 				
 				/*
 				 * since compare returns 1 if the first argument is greater then the second, 
 				 * then it implies that node's revision is to small to be placed in front of this currently present node
 				 */
 				if (list.get(i).compare(node) > 0) { 
 					i++; //if this is the case, then prepare to check the next node in the list
 				}
 				else if (list.get(i).compare(node) == 0){
 					RevisionNode thatNode = list.remove(i);
 					int j;
 					for (j = 0; j < node.getRelevantFiles().size(); j++) {
 						String file = node.getRelevantFiles().get(j);
 						if (node.getRelevantFiles().contains(file)) {
 							thatNode.newRelevantFile(file);
 						}
 					}
 					list.add(i, thatNode);
 					return;
 				}
 				else {
 					break; //if the nodes revision is greater, then it should be placed at index i, so the loop ends early
 				}
 			}
 			list.add(i, node); //place the node in it proper place
 			//if the loop fully completes then it implies that node's revision number is the smallest and so it is placed at the end
 		}
 	}
 	
 	public void linesAndBounds(String line) {
 		int j;
 		System.out.print("\n");
 		for (j = 0; j < Integer.parseInt(bundle.getString("lineLengths")); j++) { //create a line break to separate the query print out from the data table
 			System.out.print(line); //indicates the end of the list of queried files
 		}
 		System.out.print("\n"); //provide spacing between output
 	}
 
 	/**
 	 * printHistoryInformation both processes the log information of a Subversion repository through the methods above
 	 * and then prints out the names of the queried files along with all of the information stored in the RevisionNode
 	 * linked list in a table-style format
 	 * @throws Exception 
 	 */
 	public void printHistoryInformation() throws Exception{
 		
 		int i;//loop counter
 		long standard;
 		boolean inRange;
 		boolean lastWas = false;
 		
 		if (bundle.getString("queryToggle").equals("true")) {
 			System.out.println("Queried Files:"); //indicates the next lines show what was entered on the command line
 		}
 		
 		String p = bundle.getString(bundle.getString("repo")); //uses the config.properties file to get the path to the svn working copy being used
 		
 		for (i = 0; i < args.length; i++){  //loops for every specified file
 			if (bundle.getString("queryToggle").equals("true")) {
 				System.out.println("\n"+args[i]); //prints the files name and path from the start of the working copy
 			}
 			if (!args[i].startsWith("/")){ //all command line arguments must start with a / so it is checked if that is the case
 				args[i] = "/"+args[i]; //if not then the / is added to the argument at runtime
 			}
 			String n = p+args[i]; //get the path to the file in question
 			Process exec = Runtime.getRuntime().exec("svn log -v "+n/*+" -q"*/); //uses the svn's log command to get the history of the queried file
 			nodeCycle(exec, i);
 		}
 		if (bundle.getString("revisionOverallToggle").equals("true")) {
 			standard = fullTimeAverage();
 			System.out.println("\n Average Time Period Between ALL Revisions: "+standard+" hours");
 		}
 		else {
 			standard = Long.MAX_VALUE-(long)Integer.parseInt(bundle.getString("range"));
 		}
 		int j; //loop counter
 		if (bundle.getString("tableToggle").equals("true")) {
 			linesAndBounds("==========");
 			System.out.print("\n"); //provide spacing between output
 			System.out.println("Legend: ");
 			System.out.println("\t: \t\tindicates the time between this revision and the one before it is not \n\t\t\tin the selected range from the overall average\n");
 			System.out.println("\t: \t\tindicates that this revision and the one above it are in the intrval while \n\t\t\tthe current revision and the one below \n\t\t\titself is also within the specified range");
 			System.out.println("\t: \t\tindicate the bottom revision of a pair that have a time period within the \n\t\t\tdesired range but not with the one below itself");
 			System.out.println("\tA Line Of #: \tthe revisions between two of these are within the user selected \n\t\t\t interval range, starting from the most recent revision\n");
 			System.out.println("\tA Line Of -: \tthe revisions separated by these are within the user selected \n\t\t\t interval range\n");
 			System.out.print("\n"); //provide spacing between output
 			System.out.print("commit \t date \t\t\t relevants \t     changed \t rating \t\t rating comment \t\t\t\t actual relevant files");
 			linesAndBounds("##########"); //the lines used to separate the information rows
 		}
 
 		int[] statArray = new int[10];
 		double interval = 0;
 		int n = 0;
 		LinkedList<Integer> allN = new LinkedList<Integer>();
 		LinkedList<RevisionNode> history = initiallyRelevant;
 		for (i = 0; i < history.size(); i++){ //iterates through the entire RevisionNode list to print out its collected data
 			RevisionNode current = history.get(i); //takes the next node to be printed
 			fillArray(statArray, current.getRating());
 			double newSpace;
 			if (i < history.size()-1) {
 				newSpace = (double)current.getTimeSpace(history.get(i+1).getThisTime())/1000/60/60.0;
 				interval += newSpace;
 			}
 			else {
 				newSpace = 0;
 				interval = Long.MAX_VALUE;
 			}
 			if (bundle.getString("revisionOverallToggle").equals("true")) {
 				if (newSpace > (double)standard+Integer.parseInt(bundle.getString("range"))*standard/100 ||  newSpace < (double)standard-Integer.parseInt(bundle.getString("range"))*standard/100) {
 					inRange = false;
 				}
 				else {
 					inRange = true;
 				}
 			}
 			else {
 				inRange = false;
 			}
 			
 			if (i+1 == history.size() && lastWas) {
 				inRange = false;
 			}
 			else if (i+1 == history.size()) {
 				inRange = false;
 				lastWas = false;
 			}
 			
 			if (bundle.getString("tableToggle").equals("true")) {
 				if (inRange && !lastWas) {
 					System.out.print(" ");
 					lastWas = true;
 				}
 				else if (inRange && lastWas) {
 					System.out.print(" ");
 				}
 				else if (!inRange && lastWas) {
 					System.out.print(" ");
 					lastWas = false;
 				}
 				else {
 					System.out.print(" ");
 				}
 				System.out.print(current.toString()); //prints the String representation of all the nodes data
 				if (interval <= Double.parseDouble(bundle.getString("interval"))) {
 					linesAndBounds("----------"); //the lines used to separate the information rows
 				}
 				else {
 					linesAndBounds("##########");
 				}
 			}
 			
 			if (interval <= Double.parseDouble(bundle.getString("interval"))) {
 				n++;
 			}
 			else if (interval > Double.parseDouble(bundle.getString("interval"))) {
 				n++;
 				allN.addFirst(n);
 				n = 0;
 				interval = 0;
 			}
 		}
 		
 		System.out.println("\n");
 		NodeStatistics stats = new NodeStatistics(history, args, allN); // prepares to process data held in the RevisionNode list
 		stats.statsOut(); //output the statistics to the screen
 		
 		System.out.println("\n");
 		if (bundle.getString("ratingsToggle").equals("true")) {
 			System.out.println("Rating Graph: looking for grouping\n");
 			for (i = 1; i <= statArray.length; i++){
 				System.out.print("("+(double)(i-1)/10+", "+(double)i/10+"]:  "); //current range interval
 				for (j = 0; j < statArray[i-1]; j++){ //loop through the entire range of rating intervals
 					System.out.print("|"); // one '|' = one rating in this range
 				}
 				System.out.print("  ("+statArray[i-1]+")"); //print out the numerical representation of that interval for easier use
 				linesAndBounds("=========="); //indicates the end of the list of queried files
 			}
 		}
 
 		if (bundle.getString("occurrencesToggle").equals("true")) {
 			fullCount();
 		}
 	}
 	
 	public long fullTimeAverage() throws IOException {
 		String p = bundle.getString(bundle.getString("repo"));
 		String s;
 		String[] ss;
 		String previous = "";
 		String current = "";
 		long totalTime = 0;
 		int rev = 0;
 		Process exec = Runtime.getRuntime().exec("svn log "+p+" -q");
 		BufferedReader  stdInput=  new  BufferedReader(new
 	              InputStreamReader(exec.getInputStream()));
 		
 		while  ((s=  stdInput.readLine())  !=  null)  {
 			
 			if (s.startsWith("r")) {  //a line starting with a lower case r implies that we are at a new revision
 				
 				ss = s.split(" "); //split the string along white spaces
 				if (ss[4].equals("|")){
 					current = (ss[5]+" "+ss[6]); 
 				}
 				else {
 					current = (ss[4]+" "+ss[5]);  // gets both the date and time of the revision
 				}
 				Calendar thisTime = new GregorianCalendar(Integer.parseInt(current.split(" ")[0].split("-")[0]), Integer.parseInt(current.split(" ")[0].split("-")[1])-1, Integer.parseInt(current.split(" ")[0].split("-")[2]), Integer.parseInt(current.split(" ")[1].split(":")[0]), Integer.parseInt(current.split(" ")[1].split(":")[1]), Integer.parseInt(current.split(" ")[1].split(":")[2]));
 				if (!previous.equals("")){ //implies this is not the first iteration
 					Calendar lastTime = new GregorianCalendar(Integer.parseInt(previous.split(" ")[0].split("-")[0]), Integer.parseInt(previous.split(" ")[0].split("-")[1])-1, Integer.parseInt(previous.split(" ")[0].split("-")[2]), Integer.parseInt(previous.split(" ")[1].split(":")[0]), Integer.parseInt(previous.split(" ")[1].split(":")[1]), Integer.parseInt(previous.split(" ")[1].split(":")[2]));
 					long timeDiff = lastTime.getTimeInMillis()-thisTime.getTimeInMillis();
 					rev++;
 					totalTime += timeDiff;
 				}
 				previous = current;
 			}
 		}
 		return (((totalTime/rev)/1000)/60)/60;
 	}
 	
 	public void fullCount() throws IOException {
 		String p = bundle.getString(bundle.getString("repo"));
 		String s;
 		String[] ss;
 		String current = "";
 		Process exec = Runtime.getRuntime().exec("svn log "+p+" -q -v");
 		BufferedReader  stdInput=  new  BufferedReader(new
 	              InputStreamReader(exec.getInputStream()));
 		CounterList<String> counter = new CounterList<String>();
 		
 		while  ((s=  stdInput.readLine())  !=  null)  {
 			if (s.startsWith("   M") || s.startsWith("   A") || s.startsWith("   D") || s.startsWith("   R")){
 				ss = s.split(" ");
 				current = ss[4];
 				counter.newInput(current);
 			}
 		}
 		System.out.println("start");
 		System.out.println(counter.toString());
 	}
 	
 	/**
 	 * Takes the rating of a particular revision and uses it to place in an array
 	 * designed to store the rating ranges
 	 * @param array the array that stores how many ratings fall in specific value ranges
 	 * @param rating the current rating reached in  the code
 	 */
 	public void fillArray(int[] array, double rating){ 
 		int i = 0; // loop counter
 		while (i <= array.length){ //while in the bounds of the array...
 			if (rating <= (double)i/10){ // checks if this is the interval that the rating falls in
 				array[i-1] = array[i-1]+1; //if so the that interval section is incremented to indicate that change of the data
 				break; //end the loop since 1 rating is only in one interval
 			}
 			i++;
 		}
 	}
 }
