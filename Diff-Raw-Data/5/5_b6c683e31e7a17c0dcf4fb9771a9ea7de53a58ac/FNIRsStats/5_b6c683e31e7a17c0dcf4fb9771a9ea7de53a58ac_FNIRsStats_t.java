 /******************************************************************************
  * File: FNIRsStats.java                                                      *
  * Author: Nicholas Kolesar                                                   *
  * Hamilton College                                                           *
  * Fall 2013                                                                  *
  * Description: Mostly averages data sequences together. Averages             *
  *    element-wise among different sequences and among chunks of the same     *
  *    sequences, and calculates average values for all the values contained   *
  *    in chunks of the same sequence. Once these averages--grouping channels, *
  *    averaging together all sequences of data for a particular condition     *
  *    within the average sequence for each particular channel group, and      *
  *    "chunking" the resulting average sequence--are complete, ANOVA p-values *
  *    are calculated. This could be supplemented with other statistical       *
  *    analyses on the same chunked data sets later.                           *
  ******************************************************************************/
 
 // SEE ABOUT CATCHING ANY UNFORESEEN INPUTMISMATCHEXCEPTIONS FROM THE
 //    SCANNER READS LATER
 // the static keyword lets me instantiate GroupedChannels without having an 
 //    instance of HelloWorldNick first...Object Oriented is hard....
 // MAKE A NOTE SOMEWHERE APPROPRIATE THAT WE CALL THE SEQUENCE OF DATA
 //    FROM AN FNIRS CHANNEL A CHANNEL
 // CONVENTION FOR VALUES THAT SHOULD BE A SIZE_T = ?????
 // MAKE EXTRA SURE LATER ON THAT WE USE THE CORRECT LOCALE FOR ALL THE I/O
 
 package fNIRs;
 
 // (MAKE SURE WE INCLUDE THE PROPER LICENSES WITH OUR FINISHED PRODUCT)
 import org.apache.commons.math3.stat.inference.OneWayAnova;
 import java.util.Scanner; // to read doubles simply
 import java.util.Locale; // for Scanner (defines character encoding)
 import java.util.InputMismatchException;
 import java.util.ArrayList; // like C++ Vector and like Java Vector, which is
                             //    apparently deprecated
 import java.util.LinkedList; // like C++ Deque; implements List interface
 import java.util.TreeSet; // a sorted tree-based set implementation--for storing
                           //    the list of channels not in any group and the 
                           //    list of channels which are in multiple groups
 import java.util.Collections; // for max(Collection) method
 import java.io.File; // included elsewhere in project?
 import java.io.FileReader;  // for making the BufferedReader
 import java.io.FileNotFoundException; // in case creating a FileReader fails
 import java.io.BufferedReader; // for making the Scanner
 import java.io.FileWriter;
 import java.io.PrintWriter;
 //import java.io.IOException;
 //import java.util.Collection; // for sillily general Group constructor
 import java.util.List; // for generalized findNextOccurrence method
 
 public class FNIRsStats {
     private static void localError(String errMsg){
 	System.out.println("Error: " + errMsg);
         //System.exit(1); // PROBABLY DON'T WANT THIS IN FINAL PROGRAM--
                           //    HANDLE THE EXCEPTION SOME OTHER WAY<
     }
     /* makeScanner
      * IN:  a File object
      * Throws a FileNotFoundException if the file does not exist.
      * OUT: a Scanner for the input File
      */
     private static Scanner makeScanner(File file) {
         FileReader reader;
         try { // the file may or may not exist at this point
             reader = new FileReader(file);
         } catch (FileNotFoundException fnf_exception) {
             // WE WILL PROBABLY WANT TO INTEGRATE THIS INTO THE GUI
             // Print the name of the missing file:
             localError(fnf_exception.getMessage());
             return null; // never executed
         }
         // create Scanner to read easily from the input files:
         Scanner s = new Scanner(new BufferedReader(reader));
         s.useLocale(Locale.US); // tell the scanner that numbers in the input 
                                 //    are formatted with decimal points, not
                                 //    commas, etc.
         return s;
     }
     private static PrintWriter makePWriter(File file){
 	// SOMETHING ABOUT LOCALES??
 	FileWriter w = null;
     	try {
 	    w = new FileWriter(file);
     	} catch (Exception ex) {
 	    localError(ex.getMessage());
 	}
     	PrintWriter p = new PrintWriter(w, true); // set autoflush to true
 	return p;
     }
     public static void printList(List<?> lst){
         System.out.println("Index\t Value");
         for (int i = 0; i < lst.size(); i++) {
             System.out.println(i + "\t " + lst.get(i));
         }
     }
     /* processAllSubjectData
      * IN:  dataFiles, a nonempty List of data files to process together for 
      *         statistical analysis
      *      groupFile, a group file that applies to all the files in dataFiles
      * Creates a GroupedChannels object containing, for each channel grouping
      *    specified in the group file, the average sequence produced by 
      *    averaging all subjects' data for the specified channels.
      * OUT: the GroupedChannels object thus created
      */
     public static GroupedChannels processAllSubjectData(List<File> dataFiles,
 							File groupFile) {
 	// initialize output data with the data from the first data file and the
 	//    appropriate channel grouping information:
 	GroupedChannels output = new GroupedChannels(dataFiles.get(0),
 						     groupFile);
 	// iterate through the remaining subjects' data files:
 	for (File dataFile : dataFiles.subList(1, dataFiles.size())) {
 	    GroupedChannels subjectData = new GroupedChannels(dataFile,
 							      groupFile);
 	    output.combineGroups(subjectData);
 	}
 	return output;
     }
     
     // COULD OPTIMIZE BY HARDCODING THE REFLEXIVE ANOVAs
     // this should chunk the data, too
     public static void writeANOVAs(File outFile,
 				   GroupedChannels data,
 				   List<String> groupNames,
 				   List<Integer> conditions,
 				   int numChunks,
 				   int precision) {
 	// open file for writing with a nice print stream object:
 	PrintWriter ostream = makePWriter(outFile); // OKAY VAR NAME?
 	// get all condition-group sequences:
 	ArrayList<GroupedChannels.TaggedDataSequence> allTDSs =
 	    data.getAllSelectedTDSs(groupNames, conditions);
 
 	//chunkData(allTDSs, numChunks); // COMMENT THIS LATER
 	
 	int idFieldWidth = getIdFieldWidth(groupNames, conditions, precision);
 	String idFieldFormat = "%-" + idFieldWidth + "s"; // format string
 	String separator = " , "; // what goes between columns of output
 	
 	// output the first row, containing identifying information for each 
 	//    group-condition combination:
 	// first, output spaces to take the place of the identifier column:
 	ostream.printf("%" + idFieldWidth + "s" + separator, ""); // TOO HACKY??
 	// then, output all tds identifiers:
 	for (GroupedChannels.TaggedDataSequence tds : allTDSs) {
 	    ostream.printf(idFieldFormat + separator,
 			   tds.getGroupName() + " c" + tds.getCondition()); 
 	}
 	ostream.println(); // print newline
 	// output ANOVA values line by line:
 	OneWayAnova myANOVA = new OneWayAnova(); // this object will do the
                                                  //    ANOVAs
 	for (GroupedChannels.TaggedDataSequence first : allTDSs) {
 	    // output tds identifier in first column:
 	    ostream.printf(idFieldFormat + separator,
 			   first.getGroupName() +
 			   " c" + first.getCondition());
 	    // create Collection to send to the ANOVA object:
 	    LinkedList<double[]> dataSets = new LinkedList<double[]>();
 	    // convert first's data sequence to an array, then add it to
 	    //    dataSets
 	    dataSets.add(toPrimitiveDoubleArray(first.getData()));
 	    dataSets.add(null); // placeholder for second's data sequence
 	    for (GroupedChannels.TaggedDataSequence second : allTDSs) {
 		// convert and add second's data sequence to position one in
 		//    dataSets:
 		dataSets.set(1, toPrimitiveDoubleArray(second.getData()));
 		double result = 0;
 		try {
 		    result = myANOVA.anovaPValue(dataSets);
 		    // if (first == second) { // if the two TDSs are the same TDS,
 		    // 	result = 1; // then the ANOVA value should be 1, even
 			// though a divide-by-zero
 		    //		    }
 		} catch (Exception ex) {
 		    ostream.println();
 		    localError(ex.getMessage());
 		}
 		if (result != result) { // if result is NaN
 		    System.out.println("NaN on " + first.getGroupName() +
 				       " c" + first.getCondition() + " and " +
 				       second.getGroupName() + " c" +
 				       second.getCondition());
 		}
 		// AGAIN, SEE IF "PRECISON" == "NUMBER OF DECIMAL PLACES"
 		ostream.printf("%-"+idFieldWidth+"."+precision+"f"+separator,
 			       result);
 	    }
 	    ostream.println(); // print newline
 	}
 	ostream.close();
     }
     private static int getIdFieldWidth(List<String> groupNames,
 				       List<Integer> conditions,
 				       int precision) {
 	// calculate required widths for printed names and condition numbers:
 	int nameWidth = longestLength(groupNames); // length of longest name
 	int conditionWidth = // number of digits in the largest condition number
 	    String.valueOf(Collections.max(conditions)).length();
 	// make sure the fields will be wide enough to hold the ANOVA values,
 	//    which will consist of a 0 or 1 followed by a . and precision 0s:
 	// AM I USING "PRECISION" RIGHT?
 	int result = nameWidth + 2 + conditionWidth; // 2 == " c".length()
 	if (result < precision + 2) { // 2 == "1.".length()
 	    // if not, increase the condition width so the total field width is
 	    //    large enough:
 	    //System.out.println("ANOVA values are wider than identifiers.");
 	    result = precision + 2;
 	}
 	return result;
     }
     public static void outputANOVAs(GroupedChannels data,
 				    List<String> groupNames,
 				    List<Integer> conditions,
 				    int numChunks,			    
 				    int precision) {
 	// get all condition-group sequences:
 	ArrayList<GroupedChannels.TaggedDataSequence> allTDSs =
 	    data.getAllSelectedTDSs(groupNames, conditions);
 
 	chunkData(allTDSs, numChunks); // COMMENT THIS LATER
 
 	int idFieldWidth = getIdFieldWidth(groupNames, conditions, precision);
 	// create a format string for the group/condition combination identifier
 	//    fields in the output:
 	String idFieldFormat = "%-" + idFieldWidth + "s"; // format string
 	String separator = " , "; // what goes between columns of output
 	
 	// output the first row, containing identifying information for each 
 	//    group-condition combination:
 	// first, output proper-width placeholder for the identifier column:
 	System.out.printf("%" + idFieldWidth + "s" + separator, ""); // TOO HACKY??
 	// then, output all tds identifiers:
 	for (GroupedChannels.TaggedDataSequence tds : allTDSs) {
 	    System.out.printf(idFieldFormat + separator,
 			      tds.getGroupName() + " c" + tds.getCondition()); 
 	    // System.out.printf(idFieldFormat + "  ",
 	    // 		      tds.getGroupName(),
 	    // 		      tds.getCondition()); 
 	}
 	System.out.println(); // print newline
 	// output ANOVA values line by line:
 	OneWayAnova myANOVA = new OneWayAnova();
 	for (GroupedChannels.TaggedDataSequence first : allTDSs) {
 	    // output tds identifier in first column:
 	    System.out.printf(idFieldFormat + separator,
 			      first.getGroupName() +
 			      " c" + first.getCondition());
 	    // create Collection to send to the ANOVA object:
 	    LinkedList<double[]> dataSets = new LinkedList<double[]>();
 	    // convert first's data sequence to an array, then add it to
 	    //    dataSets
 	    dataSets.add(toPrimitiveDoubleArray(first.getData()));
 	    dataSets.add(null); // placeholder for second's data sequence
 	    for (GroupedChannels.TaggedDataSequence second : allTDSs) {
 		// convert and add second's data sequence to position one in
 		//    dataSets:
 		//dataSets.add(second.getData().toArray());
 		dataSets.set(1, toPrimitiveDoubleArray(second.getData()));
 		double result = 0;
 		try {
 		    result = myANOVA.anovaPValue(dataSets);
 		} catch (Exception ex) {
 		    System.out.println();
 		    localError(ex.getMessage());
 		}
 		System.out.printf("%-" + idFieldWidth + "." + precision +
 				  "f" + separator,
 				  result);
 		//dataSets.remove(1); // remove second's data from dataSets
 	    }
 	    System.out.println(); // print newline
 	}
     }
     
     public static void oldWriteANOVAs(File outFile,
 				   GroupedChannels data,
 				   List<String> groupNames,
 				   List<Integer> conditions,
 				   int numChunks,
 				   int precision) {
 	// open file for writing with a nice print stream object:
 	PrintWriter ostream = makePWriter(outFile); // OKAY VAR NAME?
 	// get all condition-group sequences:
 	ArrayList<GroupedChannels.TaggedDataSequence> allTDSs =
 	    data.getAllSelectedTDSs(groupNames, conditions);
 
 	//chunkData(allTDSs, numChunks); // COMMENT THIS LATER
 	
 	// calculate required widths for printed names and condition numbers:
 	int nameWidth = longestLength(groupNames); // length of longest name
 	int conditionWidth = // number of digits in the largest condition number
 	    String.valueOf(Collections.max(conditions)).length();
 	// make sure the fields will be wide enough to hold the ANOVA values,
 	//    which will consist of a 0 or 1 followed by a . and precision 0s:
 	int idFieldWidth = nameWidth + 2 + conditionWidth; // 2 == " c".length()
 	if (idFieldWidth < precision + 2) { // 2 == "1.".length()
 	    // if not, increase the condition width so the total field width is
 	    //    large enough:
 	    //System.out.println("ANOVA values are wider than identifiers.");
 	    idFieldWidth = precision + 2;
 	}
 	String idFieldFormat = "%-" + idFieldWidth + "s"; // format string
 	
 	// output the first row, containing identifying information for each 
 	//    group-condition combination:
 	// first, output proper-width placeholder for the identifier column:
 	ostream.printf("%" + idFieldWidth + "s  ", ""); // TOO HACKY??
 	// then, output all tds identifiers:
 	for (GroupedChannels.TaggedDataSequence tds : allTDSs) {
 	    ostream.printf(idFieldFormat + "  ",
 			   tds.getGroupName() + " c" + tds.getCondition()); 
 	    // ostream.printf(idFieldFormat + "  ",
 	    // 		      tds.getGroupName(),
 	    // 		      tds.getCondition()); 
 	}
 	ostream.println(); // print newline
 	// output ANOVA values line by line:
 	OneWayAnova myANOVA = new OneWayAnova();
 	for (GroupedChannels.TaggedDataSequence first : allTDSs) {
 	    // output tds identifier in first column:
 	    ostream.printf(idFieldFormat + "  ",
 			   first.getGroupName() +
 			   " c" + first.getCondition());
 	    // create Collection to send to the ANOVA object:
 	    LinkedList<double[]> dataSets = new LinkedList<double[]>();
 	    // convert first's data sequence to an array, then add it to
 	    //    dataSets
 	    dataSets.add(toPrimitiveDoubleArray(first.getData()));
 	    dataSets.add(null); // placeholder for second's data sequence
 	    for (GroupedChannels.TaggedDataSequence second : allTDSs) {
 		// convert and add second's data sequence to position one in
 		//    dataSets:
 		dataSets.set(1,toPrimitiveDoubleArray(second.getData()));
 		double result = 0;
 		try {
 		    result = myANOVA.anovaPValue(dataSets);
 		    // if (first == second) { // if the two TDSs are the same TDS,
 		    // 	result = 1; // then the ANOVA value should be 1, even
 			// though a divide-by-zero
 		    //		    }
 		} catch (Exception ex) {
 		    ostream.println();
 		    localError(ex.getMessage());
 		}
 		if (result != result) { // if result is NaN
 		    System.out.println("NaN on " + first.getGroupName() +
 				       " c" + first.getCondition() + " and " +
 				       second.getGroupName() + " c" +
 				       second.getCondition());
 		}
 		ostream.printf("%-" + idFieldWidth + "." + precision + "f  ",
 				  result);
 	    }
 	    ostream.println(); // print newline
 	}
 	ostream.close();
     }
     // this should chunk the data, too
     public static void oldOutputANOVAs(GroupedChannels data,
 				    List<String> groupNames,
 				    List<Integer> conditions,
 				    int numChunks,			    
 				    int precision) {
 	// get all condition-group sequences:
 	ArrayList<GroupedChannels.TaggedDataSequence> allTDSs =
 	    data.getAllSelectedTDSs(groupNames, conditions);
 
 	chunkData(allTDSs, numChunks); // COMMENT THIS LATER
 	
 	// calculate required widths for printed names and condition numbers:
 	int nameWidth = longestLength(groupNames); // length of longest name
 	int conditionWidth = // number of digits in the largest condition number
 	    String.valueOf(Collections.max(conditions)).length();
 	// make sure the fields will be wide enough to hold the ANOVA values,
 	//    which will, in the widest case, consist of a 1 followed by a . and
 	//    precision 0s: // AM I USING "PRECISION" RIGHT?
 	int idFieldWidth = nameWidth + conditionWidth + 2;
 	if (idFieldWidth < precision + 2) { // for the "1."
 	    // if not, increase the condition width so the total field width is
 	    //    large enough:
 	    idFieldWidth = precision + 2;
 	}
 
 	String idFieldFormat = "%-" + idFieldWidth + "s";
 	
 	// output the first row, containing identifying information for each 
 	//    group-condition combination:
 	// first, output proper-width placeholder for the identifier column:
 	System.out.printf("%" + idFieldWidth + "s  ", ""); // TOO HACKY??
 	// then, output all tds identifiers:
 	for (GroupedChannels.TaggedDataSequence tds : allTDSs) {
 	    System.out.printf(idFieldFormat + "  ",
 			      tds.getGroupName() + " c" + tds.getCondition()); 
 	}
 	System.out.println(); // print newline
 	// output ANOVA values line by line:
 	OneWayAnova myANOVA = new OneWayAnova();
 	for (GroupedChannels.TaggedDataSequence first : allTDSs) {
 	    // output tds identifier in first column:
 	    System.out.printf(idFieldFormat + "  ",
 			      first.getGroupName() +
 			      " c" + first.getCondition());
 	    // create Collection to send to the ANOVA object:
 	    LinkedList<double[]> dataSets = new LinkedList<double[]>();
 	    // convert first's data sequence to an array, then add it to
 	    //    dataSets
 	    dataSets.add(toPrimitiveDoubleArray(first.getData()));
 	    dataSets.add(null); // placeholder for second's data sequence
 	    for (GroupedChannels.TaggedDataSequence second : allTDSs) {
 		// convert and add second's data sequence to position one in
 		//    dataSets:
 		dataSets.set(1,
 			     toPrimitiveDoubleArray(second.getData())
 			     );
 		double result = 0;
 		try {
 		    result = myANOVA.anovaPValue(dataSets);
 		} catch (Exception ex) {
 		    System.out.println();
 		    localError(ex.getMessage());
 		}
 		System.out.printf("%-" + idFieldWidth + "." + precision + "f  ",
 				  result);
 		//dataSets.remove(1); // remove second's data from dataSets
 	    }
 	    System.out.println(); // print newline
 	}
     }
     public static void chunkData(List<GroupedChannels.TaggedDataSequence> data,
 			  int numChunks) {
 	for (GroupedChannels.TaggedDataSequence tds : data) {
 	    averageChunks(tds.getData(), numChunks);
 	}
     }    
     // public static void
     // 	chunkData(List<List<GroupedChannels.TaggedDataSequence>> data,
     // 		  int numChunks) {
     // 	for (List<GroupedChannels.TaggedDataSequence> lst : data) {
     // 	    //for (List<GroupedChannels.TaggedDataSequence> inner : outer) {
     // 	    for (GroupedChannels.TaggedDataSequence tds : lst) {
     // 		averageChunks(tds.getData(), numChunks);
     // 	    }
     // 	    //}
     // 	}
     // }
     /* longestLength
      * IN:  lst, a List of Strings
      * OUT: returns the length of the longest string in lst
      */
     private static int longestLength(List<String> lst) {
 	int longest = 0;
 	for (String s : lst) {
 	    if (s.length() > longest) {
 		longest = s.length();
 	    }
 	}
 	return longest;
     }
     /* combineArrayLists
      * IN:  lst_of_lsts, an List of Lists
      * OUT: returns a list containing all the elements in lst_of_lsts's sublists
      */
     private static <T> ArrayList<T> combineArrayLists(ArrayList<ArrayList<T>> lst_of_lsts) {
 	ArrayList<T> output = new ArrayList<T>();
 	for (ArrayList<T> lst : lst_of_lsts) {
 	    output.addAll(lst);
 	}
 	return output;
     }    
     /* combineLists
      * IN:  lst_of_lsts, a List of Lists
      * OUT: returns a list containing all the elements in lst_of_lsts's sublists
      */
     private static double[] toPrimitiveDoubleArray(ArrayList<Double> lst){
 	double[] result = new double[lst.size()];
 	for (int i = 0; i < lst.size(); i++) {
 	    result[i] = lst.get(i);	    
 	}
 	return result;
     }
     // CALL THIS "BLOCK AVERAGE" OR SOMETHING?
     public static ArrayList<Double> averageChunks(List<Double> ary,
 						  int numChunks){
 	ArrayList<Double> result = new ArrayList<Double>();
 	double chunkSize = ((double) ary.size()) / (double) numChunks;
 	int start = 0; // index of start of current chunk
 	double bound = 0; // decimal value of upper bound for current chunk
 	while(start < ary.size()){ // REDUNDANT??
 	    bound += chunkSize; // get upper bound for next chunk
 	    double sum = 0; // to store sum of values in the chunk
 	    double numValues = 0; // number of values in the chunk (to divide by
                                   //    to get the average value for the chunk)
 	    for (int i = start; i < bound && i < ary.size(); i++){
 		sum += ary.get(i);
 		numValues++;
 	    }
 	    result.add(sum / numValues);
 	    start += numValues;
 	}
 	return result;
     }
     /* GroupedChannels class
      * This class is a container for all the data read from an Hb or HbO file,
      *    organized within channel groupings represented by Group objects.
      */
     public static class GroupedChannels { // BETTER NAME PROBABLY
         /* GroupedChannels
          * IN:  groupFile, a File specifying channel groupings for statistical 
          *         analysis. Format is a string to name the grouping, then 
          *         natural numbers identifying the channels in that group, 
          *         terminated by the next grouping's name or EOF.
          *      dataFile, a File containing whitespace-delimited columns of
 	 *         preprocessed Hb or HbO values, which correspond to channels
 	 *         from the fNIRs device. The last column must contain an
 	 *         integer condition number for each (newline-delimited) row.
          * This constructor creates all the necessary Groups to read from an Hb
          *    or HbO file and checks to see if any channels are specified to be
          *    in multiple groups or not specified to be in a group.
          */
         public GroupedChannels (File data, File groups) {
             NumChannels = calcNumChannels(data); // find and store the number of
                                                  //    channels in the Hb/HbO
                                                  //    file
             GroupList = new ArrayList<Group>(); // initialize the list of Groups
 	    Conditions = new ArrayList<Integer>(); // initialize the list of
                                                    //    conditions
 	    DuplicatedChannels = new TreeSet<Integer>(); // initialize set of
                                                         //    channels in
                                                         //    multiple groups
 	    MissingChannels = new TreeSet<Integer>(); // initialize set of
                                                       //    channels in no group
             makeGroups(groups); // create a Group for each name and set of
                                 //    channels in the group file
             readData(data); // read the data file and store its values as
                             //    channel grouping average sequences in the
                             //    groups just created
         }
 	/* calcNumChannels()
 	 * IN:  dataFile, a File containing whitespace-delimited columns of
 	 *         preprocessed Hb or HbO values, which correspond to channels
 	 *         from the fNIRs device. The last column must contain an
 	 *         integer condition number for each (newline-delimited) row.
          * OUT: numChannels, the number of channels the Hb or HbO input file 
          *         has (not counting the extra condition column)
 	 */
 	private int calcNumChannels(File dataFile) {
 	    Scanner s = makeScanner(dataFile);
 	    // read one line from the file, then remove leading and trailing
 	    //    whitespace:
 	    String line = s.nextLine().trim();
 	    // produce an array by splitting line on whitespace characters:
 	    String[] columnValues = line.split("\\s+"); // regular expression
 	    // the number of channels is the number of columns in the input file
 	    //    minus one, because the last column in each row contains the
 	    //    condition number for the row:
 	    return columnValues.length - 1; 
 	}
         /* makeGroups
          * IN:  groupFile, A file specifying channel groupings for statistical 
          *         analysis. Format is a string to name the grouping, then 
          *         natural numbers identifying the channels in that group, 
          *         terminated by the next grouping's name or EOF.
          * Checks to see if any channels are specified to be in multiple groups 
          *    or not specified to be in any group.
          */
         private void makeGroups(File groupFile){
             Scanner s = makeScanner(groupFile);
             String groupName;
             ArrayList<Integer> channels = new ArrayList<Integer>();
             while(s.hasNext()) { // checks to see there is a token in the input
                 groupName = s.next(); // next() gets the next token
                 while (s.hasNextInt()) {
                     channels.add(s.nextInt());
                 }
                 GroupList.add(new Group(groupName, channels));
                 channels.clear(); // empty list for next iteration
             }
             s.close(); // scanner must be closed to signal it's okay to 
                        //    close its underlying stream
             checkForMissingChannels();
         }
         private void checkForMissingChannels(){
             for (int chan = 1; chan <= NumChannels; chan++) {
                 int found = 0;  // number of the Groups that contain the channel
                 for (Group g : GroupList) {
                     if (g.hasChannel(chan)){
                         found++;
                     }
                 }
                 switch (found) {
                 case 0: // channel is missing from all groups
                     MissingChannels.add(chan);
                     break;
                 case 1: // all is well
                     break;
                 default:
                     DuplicatedChannels.add(chan);
                     break;
                 }
             }
 	    // output errors if the program is running with a console:
             if (!DuplicatedChannels.isEmpty()) {
                 // THIS IS STATISTICALLY PRETTY BAD UNLESS THEY'RE NOT COMPARING
                 //    THOSE TWO GROUPS...WHICH IS TOTALLY POSSIBLE, I GUESS.
                 //    MAYBE THIS IS BEYOND THE SCOPE OF OUR PROJECT TO CHECK?
 		localError(getDuplicatedChannelsMsg());
             } else {
 		// remember there are no duplicated channels:
 		DuplicatedChannels = null; 
 	    }
             if (!MissingChannels.isEmpty()) {
 		// remember there are no missing channels:		
 		localError(getMissingChannelsMsg());
 	    } else {
 		MissingChannels = null;
 	    }
 	}
 	boolean channelsMissing() {
	    return (MissingChannels != null);
 	}
 	boolean channelsDuplicated() {
	    return (DuplicatedChannels != null);
 	}	
 	TreeSet<Integer> getMissingChannels() {
 	    return MissingChannels;
 	}
 	TreeSet<Integer> getDuplicatedChannels() {
 	    return DuplicatedChannels;
 	}
 	String getMissingChannelsMsg() {
 	    if (MissingChannels.isEmpty()) {
 		return null;
 	    }
 	    String msg = null;
 	    if (MissingChannels.size() == 1) {
 		msg = "Channel " + MissingChannels.first() + " is";
 	    } else {
 		msg = "Channels ";
 		for (Integer i : MissingChannels) {
 		    if (i == MissingChannels.last()) {
 			msg += ", and ";
 		    } else if (i != MissingChannels.first()) {
 			msg += ", ";
 		    }
 		    msg += i;
 		}
 		msg += " are";
 	    }
 	    msg += " not in any group.";
 	    return msg;
 	}
 	String getDuplicatedChannelsMsg() {
 	    if (DuplicatedChannels.isEmpty()) {
 		return null;
 	    }
 	    String msg = null;
 	    if (DuplicatedChannels.size() == 1) {	    
 		msg = "Channel " + DuplicatedChannels.first() + " is";	    
 	    } else {
 		msg = "Channels ";
 		for (Integer i : DuplicatedChannels) {
 		    if (i == DuplicatedChannels.last()) {
 			msg += ", and ";
 		    } else if (i != DuplicatedChannels.first()) {
 			msg += ", ";
 		    }
 		    msg += i;
 		}
 		msg += " are";
 	    }
 	    msg += " in multiple groups!";
 	    return msg;
 	}
         /* readData
          * IN:  dataFile, a preprocessed Hb or HbO file
          * Reads all the data from the file, averaging channels' values together
          *    to produce group average value(s) for each row, which are stored
          *    along with the condition that row was part of.
          */
         private void readData(File dataFile) {
             Scanner s = makeScanner(dataFile);
             try { // in case the input doesn't match the specification
                 while(s.hasNextDouble()){ //while there is another row of values
                     Double[] values = new Double[NumChannels];
                     for (int channel = 0; channel < NumChannels; channel++){
                         values[channel] = s.nextDouble(); // read all data
                     }
                     int condition = s.nextInt(); // read condition for this row
 		    if (!Conditions.contains(condition)) 
 			Conditions.add(condition); // store condition number
                     for (int channel = 1; channel <= NumChannels; channel++){
                         for (Group g : GroupList){
                             if (g.hasChannel(channel)){
                                 g.addValue(values[channel-1], condition);
                             }
                         }
                     }
                 }
             } catch (InputMismatchException e) {
                 localError(e.getMessage());
             } catch (Exception e){
                 localError(e.getMessage());
             } finally {
                 s.close(); // scanner must be closed to signal it's okay to 
                            //    close its underlying stream
             }
         }
 	// public void outputData(GroupedChannels data,
 	// 		       List<String> groups,
 	// 		       List<Integer> conditions) {
 	//     // retrieve data for the specified groups and conditions:
 
 	//     ArrayList<ArrayList<GroupedChannels.TaggedDataSequence>> data =
 	// 	getData(groups, conditions);
 	
 	// }
 	/* combineGroups
          * IN:  other, a GroupedChannels object whose data should be merged into
          *         this object's channel grouping average sequences
          * OUT: The groups in this object which have the same set of channels as
          *         a group in other have data sequences which are the length of
          *         the shorter of the two original sequences. They contain the 
          *         averages of all the channels in both groupings as if they 
          *         were averaged together into the same sequence instead of
 	 *         being put into separate Groups.
          */
         public void combineGroups(GroupedChannels other){
 	    if (other == this)
 		localError("?????"); // MAYBE FIX THIS OR SOMETHING
 	    for (Group g : other.getGroups()) {
 		// add g's data to the average sequence in the group that
 		//    matches g in this object:
 		combineGroups(g);
 	    }
         }
         /* combineGroups
          * IN:  other, another Group whose data should be merged into the
          *         average sequence of the appropriate Group in this collection
          *         of channel groupings
          * OUT: The group in this class with the same set of channels as other
          *         has a data sequence the length of the shorter two original
          *         sequences. It contains the average of all the channels in
          *         both groupings as if they were averaged together into the
          *         same sequence instead of being put into separate Groups.
          */
 	// broken :(
 	// only works for combining two groups!
         public void combineGroups(Group other){
             // Make sure we have a group with the same channels as the
             //    specified one:
             Group ours = getGroup(other.getChannels());
 	    // (MAKE THIS ERROR BETTER)
             if (ours == null)
                 localError("No other group contains those channels.");
             // See which group has a shorter data sequence:
             int combinedSize;
             if (other.getData().size() > ours.getData().size())
                 combinedSize = ours.getData().size();
             else { // our group's average sequence is too long
                 combinedSize = other.getData().size();
                 removeAfter(ours.getData(), combinedSize); // BAD???
             }
             for (int i = 0; i < combinedSize; i++){
                 double ourSum =
                     ours.getData().get(i) *
 		    ours.getNumChannels() *
 		    ours.NumSubjects;
                 double otherSum =
                     other.getData().get(i) * other.getNumChannels();
                 //double totalNumChannels = ours.getNumChannels() + other.getNumChannels();
                 //Double combinedAverage = (ourSum + otherSum) / totalNumChannels / (ours.NumSubjects + 1);
                 Double combinedAverage =
 		    (ourSum + otherSum) / (ours.NumSubjects + 1); 
                 ours.getData().set(i, combinedAverage);
             }
 	    ours.NumSubjects++; // since we've just added another subject's data
                                 //    to this group's
         }
         /* removeAfter
          * IN:  ary, a List to remove elements from
          *      startIndex, an index at which to start removing elements
          *         from the array
          * OUT: ary contains all its old elements up to (startIndex - 1)
          */
         private void removeAfter(List<?> ary, int startIndex){
             ary.subList(startIndex, ary.size()).clear();
         }
 	/* getAllTDSs
 	 * IN:  none
 	 * OUT: returns an ArrayList containing all TaggedDataSequences 
 	 *         specified by a condition-group combination contained by the
 	 *         object
 	 */
 	public ArrayList<TaggedDataSequence> getAllTDSs() {
 	    ArrayList<String> groupNames = getGroupNames();
 	    ArrayList<Integer> conditions = getConditions();
 	    return combineArrayLists(selectData(groupNames, conditions));
 	}
 	/* getAllSelectedTDSs
 	 * IN:  groups, a list of group names (strings)
 	 *      conditions, a list of condition identifiers (ints)
 	 * OUT: returns an ArrayList containing all TaggedDataSequences 
 	 *         specified by a condition-group combination from the two input
 	 *         lists.
 	 */
 	public ArrayList<GroupedChannels.TaggedDataSequence>
 	    getAllSelectedTDSs(List<String> groupNames,
 			       List<Integer> conditions) {
 	    return combineArrayLists(selectData(groupNames, conditions));
 	}
 	/* selectData
 	 * IN:  groupNames, a list of group names (strings)
 	 *      conditions, a list of condition identifiers (ints)
 	 * OUT: returns an ArrayList of ArrayLists of TaggedDataSequences. This
 	 *         collection of tagged data sequences contains every data
 	 *         sequence specified by a condition-group combination from the
 	 *         two input lists.
 	 * Note: Each value for the first dimension of the 2D list returned 
 	 *    corresponds to a group, and each value for the second dimension of
 	 *    the list corresponds to a condition. Each TaggedDataSequence also
 	 *    contains the name of the group and the number of the condition
 	 *    that produced it.
 	 */
 	// THIS WILL ALLOW US TO HAVE CHECK BOXES FOR THE GROUPS AND CONDITIONS,
 	//    WHICH IS SUPER NEAT IF IT'S ACTUALLY USEFUL--ASK LEANNE!	
         public ArrayList<ArrayList<TaggedDataSequence>>
            selectData(List<String> groupNames, List<Integer> conditions){
             // AT CERTAIN POINTS, IT SEEMS LIKE I SHOULD BE USING A LINKED,
             //    RATHER THAN ARRAY-BASED, STRUCTURE?
 
             // Initialize two-dimensional array of tagged sequences:
             ArrayList<ArrayList<TaggedDataSequence>> result =
                 new ArrayList<ArrayList<TaggedDataSequence>>();
 
             // Iterate through selected groups:
 	    for (String groupName : groupNames) {
                 Group group = getGroup(groupName);
                 // Declare the new one-dimensional array of tagged
                 //    sequences:
                 ArrayList<TaggedDataSequence> temp =
                     new ArrayList<TaggedDataSequence>();
                 // Iterate through conditions:
 		for (Integer condition : conditions) {
                     // get the data from the group for the given condition,
                     //    adding it to the temporary 1D result list
                     temp.add(new TaggedDataSequence(group,
                                                     condition,
                                                     group.getData(condition
                                                                   )));
 		    
 		}
                 // put list of results for this group in the result 2D list
 		result.add(temp); 		
 	    }
 	    return result;
 	}
 	class TaggedDataSequence{
             TaggedDataSequence(Group sourceGroup,
                                int condition,
                                ArrayList<Double> data){
                 SourceGroup = sourceGroup;
                 Condition = condition;
                 Data = data;
             }
             public String getGroupName(){ return SourceGroup.getName(); }
             public ArrayList<Double> getData(){ return Data; }
             public int getCondition(){ return Condition; }
             public void print(){
                 System.out.println("Data from group \"" + getGroupName() +
                                    "\" under condition " + getCondition() + ":"
 				   );
                 System.out.println("\t Row   Value ");
                 for (int i = 0; i < Data.size(); i++)
                     System.out.println("\t " + i + "\t " + Data.get(i));
             }
             private ArrayList<Double> Data;
             private Group SourceGroup;
             private int Condition;
         }
 	/* getGroupNames
 	 * IN:  none
 	 * OUT: returns an ArrayList containing the names of all the channel 
 	 *         groupings.
 	 */
 	public ArrayList<String> getGroupNames() {
 	    ArrayList<String> groupNames = new ArrayList<String>();
 	    for (Group g : GroupList) {
 		groupNames.add(g.getName());
 	    }
 	    return groupNames;
 	}
 	/* getGroupNames
 	 * IN:  none
 	 * OUT: returns an ArrayList containing the names of all the channel 
 	 *         groupings.
 	 */	
 	public ArrayList<Integer> getConditions() {
 	    return new ArrayList<Integer> (Conditions);	    
 	}
         /* getGroup
          * IN:  channels, a List of channels the group should be assigned
          * OUT: returns a reference to group whose set of assigned channels is
          *         channels
          */
         public Group getGroup(List<Integer> channels){
             for (Group g : GroupList)
                 if (g.sameChannels(channels))
                     return g;
             return null;
         }
         /* getGroup
          * IN:  groupNum, the number of a group, starting from 1, in the order
          *         the groups were specified
          * OUT: a reference to the specified group
          */
         public Group getGroup(int groupNum){
             if (1 <= groupNum && groupNum <= GroupList.size())
                 return GroupList.get(groupNum - 1);
             else {
                 localError("Group number " + groupNum + " not found");
                 return null; // never executed
             }
         }
         /* getGroup
          * IN:  name, the name of a channel grouping
          * OUT: a reference to the specified group         
          */
         public Group getGroup(String name){
             for (Group g : GroupList) {
                 if (name.equals(g.getName())){
                     return g;
                 }
             }
             localError("Group \"" + name + "\" not found");
             return null; // never executed
         }
         public void print(){
             System.out.println("Groups:");
             for (Group g : GroupList) {
                 System.out.print("\t");
                 g.printChannels();
                 System.out.print("\t");                
                 g.printData();
                 g.printConditions();
                 if (g != GroupList.get(GroupList.size()-1))
                     System.out.println("\t-----------------------------------");
             }
         }
 	public ArrayList<Group> getGroups() {
 	    return new ArrayList<Group>(GroupList);
 	}
         public void print(String groupName){
             getGroup(groupName).print();
         }
         public void printChannels(){
             System.out.println("Groups:");
             for (Group g : GroupList) {
                 System.out.print("\t");
                 g.printChannels();
             }
         }
 
         private class Group {
             public Group(String name, ArrayList<Integer> channels){ 
                 Name = name;
                 DataSequence = new ArrayList<Double>();
                 Condition = new ArrayList<Integer>();
                 Channels = new ArrayList<Integer>(channels); // copy channels 
                                                              //    into Channels
                 NumChannels = channels.size();
                 Sum = 0; // for addValue
                 NumVals = 0; // for addValue
 		NumSubjects = 1; // for combineGroups
             }
             /* addValue
              * IN:  value, an Hb/HbO value to be added to the Group's sequence 
              *         of averages
              *      condition, the experimental condition associated with the
              *         value
              * Adds the value to the running sum for the current row, and, if
              *    enough values have been added to account for all the channels
              *    in the group, then they are averaged and the average value is
              *    added to the sequence.
              * OUT: If an average value was calculated, Sum is 0. Else it now
              *         contains value.
              *      If an average value was calcluated, NumVals is 0. Else it is
              *         increased by 1 to account for the new value.
              *      Condition's value for the current row is set to condition.
              * Note: Sum is initialized to 0 in the constructor.
              */
             public void addValue(double value, int condition){ // BETTER NAME?
                 assert(NumVals < NumChannels);
                 if (NumVals == 0) // if this is the first value for the current 
                     Condition.add(condition); // row, then set row's condition
                 //    number
                 Sum += value; // add this value to the total
                 NumVals++; // increase the count of values in the sum
                 // if we now have enough data to add another average value...
                 if(NumVals == NumChannels) { // ...to the time series,
                     // then average & add it:
                     DataSequence.add(Sum / ((double) NumChannels));
                     Sum = 0; // reset Sum... 
                     NumVals = 0; // ...and the number of values in it
                 }
             }
             public void print(){
                 System.out.print("Group \"" + getName() +
                                  "\" has channels: \t");
                 printChannels();
                 System.out.println("\tData: \tRow \tValue    Condition");
                 for (int i = 0; i < DataSequence.size(); i++)
                     System.out.println("\t\t " + i +
                                        "\t " + DataSequence.get(i) +
                                        "\t  " + Condition.get(i));
             }
             /* printChannels
              * IN: none
              * OUT: prints the name of the group and its channels, which are
              *         tabbed in so that groups' channels can easily be compared
              *         when printed on separate lines
              */
             public void printChannels(){
                 for (int i = 0; i < this.getNumChannels(); i++){
                     System.out.print(Channels.get(i));
                     if (i < this.getNumChannels() - 1)
                         System.out.print(" ");
                 }
                 System.out.println();
             }
             /* printData
              * IN: none
              * OUT: prints the name of the group followed by its entire data 
              *         sequence, which begins tabbed in so that, for small test 
              *         data sequences, groups' data sequences can easily be 
              *         compared when printed on separate lines
              */
             public void printData(){
                 for (Double i : DataSequence){
                     System.out.print(i + " ");
                 }
                 System.out.println();
             }
             /* printConditions
              * IN:  none
              * OUT: prints the name of the group followed by its condition
              *         sequence, which is preceded by a tab character so it
              *         lines up with that of another group printed on a
              *         separate line
              */
             public void printConditions(){
                 for (int c : Condition){
                     System.out.print(c + " ");
                 }
                 System.out.println();
                 System.out.print("\t\t\t\t\t");
                 System.out.print("\n");
             }
             public String getName() {
                 return Name;
             }
             public int getNumChannels() {
                 return NumChannels;
             }
             /* sameChannels
              * IN:  other, a reference to a Group
              * OUT: returns true iff other and this group have the same
              *         channels assigned to them
              */
             public Boolean sameChannels(Group other){
                 ArrayList<Integer> othersChannels = other.getChannels();
                 // see if other's channels are a subset of ours
                 for (Integer chan : othersChannels){ 
                     if (!Channels.contains(chan))
                         return false;
                 }
                 // see if our channels are a subset of other's
                 for (Integer chan : Channels){
                     if (!othersChannels.contains(chan))
                         return false;
                 }
                 // if both are subsets of each other, then they are equal
                 return true;
             }
             /* sameChannels
              * IN:  channels, a list of channels (Integers)
              * OUT: returns true iff the set of channels assigned to this group
              *         is channels
              */
             public Boolean sameChannels(List<Integer> channels){
                 // see if channels is a subset of our set of channels:
                 for (Integer chan : channels){ 
                     if (!Channels.contains(chan))
                         return false;
                 }
                 // see if our set of channels is a subset of channels:
                 for (Integer chan : Channels){
                     if (!channels.contains(chan))
                         return false;
                 }
                 // if both are subsets of each other, then they are equal
                 return true;
             }            
             /* getChannels
              * IN:  none
              * OUT: returns an ArrayList of channels contained in the Group
              */
             public ArrayList<Integer> getChannels(){
                 return new ArrayList<Integer>(Channels); // deep copy
             }
             /* hasChannel
              * IN:  channel, an integer specifying an fNIRs device channel in
              *         the input file (actually, could be any int)
              * OUT: returns true iff the channel is in this channel grouping
              */
             public boolean hasChannel(int channel){
                 return Channels.contains(channel);
             }
             /* getData
              * IN:  none
              * OUT: returns this group's data sequence
              */
             public ArrayList<Double> getData(){
                 return DataSequence;
             }
             /* getData
              * IN:  condition, the number of the condition whose trials are to
              *         be averaged
              * OUT: returns an ArrayList containing the average of the first
              *         rows of all occurrences of condition in the input, the
              *         average of the second rows, etc., with the resulting
              *         ArrayList only as long as the shortest occurrence. That
              *         is, it returns a data sequence that is the average of all
              *         the times the experimental condition occurred in the 
              *         input sequence.
              *      returns an empty ArrayList if there were no occurrences of
              *         the condition in the original input Hb or HbO file
              */
             public ArrayList<Double> getData(int condition) {
                 ArrayList<Double> avgSequence = new ArrayList<Double>();
                 int numOccurrences = 0; // number of separate times we've found 
                                         //    the condition in the data sequence
                                         //    (for calculating the averages)
                 int i = 0;
                 while (true){
                     // length of current occurrence of condition:
                     int numRowsFound = 0; 
                     // find the starting index of the next occurrence of 
                     //    condition in the sequence:
                     i = myFindNextOccurrence(i, condition);
                     if (i == -1) { // -1 returned if not found
                         // If we found at least one occurrence of the
                         //    condition:
                         if (!avgSequence.isEmpty())
                             // To average, divide the sum in each row by the
                             //    number of occurrences of the condition
                             //    that have been found:
                             for (int j = 0; j < avgSequence.size(); j++)
                                 avgSequence.set(j,(avgSequence.get(j) /
                                                    (double) numOccurrences));
                         // return (possibly empty) avgSequence: 
                         return avgSequence; 
                     }
                     do {
                         // If this is the first occurrence of the condition,
                         if (numOccurrences == 0) {
                             // add each row of this occurrence of the condition 
                             //    to the average by simply copying its value to 
                             //    the average sequence as a new element:
                             avgSequence.add(DataSequence.get(i));
                             // Else, if this occurrence of the condition is 
                             //    longer than the shortest one found so far,    
                         } else if (numRowsFound >= avgSequence.size()){
 			    // System.out.println("avgSequence: ");
 			    // printList(avgSequence);
                             i = skip(i); // skip the rest of it...
                             break; // and find the next occurrence of the
                                    //    condition 
                         } else {
                             // add each row of this occurrence of the condition
                             //    to the average by adding it to the running sum
                             //    contained in that row of the average sequence:
                             avgSequence.set(numRowsFound,
                                             (avgSequence.get(numRowsFound) +
                                              DataSequence.get(i)));
                         }
                         numRowsFound++;
                         // move on to the next row until we reach the next
                         //    condition or there are no more rows:
                         i++;
                     } while ((i < Condition.size()) && 
                              (Condition.get(i) == condition)); 
                     // If this interval was shorter than the others:
                     if (numRowsFound < avgSequence.size())
                         // shorten the average sequence to match this one's
                         //    length (by removing the extra trailing elements):
                         removeAfter(avgSequence, numRowsFound);
                     // avgSequence.subList(numRowsFound,
                     //                        avgSequence.size()).clear();
                     numOccurrences++;
                 }
             }
             private int myFindNextOccurrence(int startIndex, int condition){
                 if (startIndex >= Condition.size() - 1)
                     return -1;
                 while (Condition.get(startIndex) != condition) {
                     if (startIndex >= Condition.size() - 1)
                         return -1;
                     startIndex++;                    
                 }
                 return startIndex;
             }
             /* findNextOccurrence
              * IN:  lst, a collection that implements the List interface (???)
              *      startIndex, the first index in the List to check
              *      value, the value to search lst for
              * OUT: returns the first index, starting from startIndex, of value
              *         in lst
              *      returns -1 if value is not found in lst (because indexOf 
              *         does)
              */
 	    /*
             private <T> int findNextOccurrence(List<T> lst,
                                                int startIndex,
                                                T value){
                 // get the sub-list that we want to search for the value:
                 List<T> toSearch = lst.subList(startIndex, lst.size());
                 return startIndex + // since the array we're searching starts at
                                     //    this index in the whole array
                     toSearch.indexOf(value); // first index of value in toSearch
             }
 	    */
             /* skip
              * IN:  startIndex, the index of the condition to skip
              * OUT: returns the index of the first row in the data sequence that
              *         is not part of an occurrence of the condition
              */
             private int skip(int startIndex) {
 		//System.out.print("Skipping past index: ");
 		//System.out.println(startIndex);
                 // store the condition to skip:
                 int condition = Condition.get(startIndex);
                 // increment the index until we pass the current condition:
                 while (Condition.get(++startIndex) == condition);
 		//System.out.print("Index is now: ");
 		//System.out.println(startIndex);
                 return startIndex;
             }
 	    // class variables for Group:
             private final String Name; 
             private final ArrayList<Integer> Channels;
             private double Sum;
             private ArrayList<Double> DataSequence; // contains actual "time
                                                     //    series" data
             private ArrayList<Integer> Condition; // stores a condition 
                                                   //    associated with every
                                                   //    row of the time series
             private final int NumChannels;
             private int NumVals;
 	    private int NumSubjects;
         }
 	
 	// class variables for GroupedChannels:
         private final int NumChannels;
         private ArrayList<Group> GroupList;
 	private ArrayList<Integer> Conditions;
 	private TreeSet<Integer> MissingChannels;
 	private TreeSet<Integer> DuplicatedChannels;
     }
     private static void runChunkTests(){
 	System.out.println("Running chunk averaging tests.");
 	chunkTest("testData1", 3); 
 	chunkTest("testData1", 4);
 	chunkTest("testData2", 3);
 	chunkTest("testData3", 3);
 	chunkTest("testData1", 2);		
 	System.out.println("Done testing.");
     }
     private static void chunkTest(String data, int numChunks){
 	File groupFile = new File("c:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/ChunkingTests/testGroups");
 	File dataFile = new File("C:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/ChunkingTests/" + data);
         System.out.println("Reading group information from " + data + ".");
         GroupedChannels groups = new GroupedChannels(dataFile, groupFile);
 	System.out.println("Average sequence before averaging:");
 	ArrayList<Double> output = groups.getGroup("foo").getData(1);
 	printList(output);
 	
 	// test: 
 
 	output = averageChunks(groups.getGroup("foo").getData(1), numChunks);
 	System.out.println("Average sequence after averaging " + numChunks +
 			   " chunks together:");
 	printList(output);
     }
     private static void runSubjectTests() {
 	// test subject combining functions:
 
 	// test files:
 	File groupFile = new File("c:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/SubjectTests/groups");
 	File data1 = new File("C:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/SubjectTests/testData1");
 	File data2 = new File("C:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/SubjectTests/testData2");
 	File data3 = new File("C:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/SubjectTests/testData3");
 
         // read channel grouping information:
         System.out.println("Reading group information.");
         GroupedChannels groups1 = new GroupedChannels(data1, groupFile);
 	GroupedChannels groups2 = new GroupedChannels(data2, groupFile);
 	GroupedChannels groups3 = new GroupedChannels(data3, groupFile);
         System.out.println("Done reading group information.");
 
 	// display contents of groups before combining:
 	System.out.println("groups1:");
 	printList(groups1.getGroup("foo").getData(1));
 	System.out.println("groups2:");	
 	printList(groups2.getGroup("foo").getData(1));
 	System.out.println("groups3:");	
 	printList(groups3.getGroup("foo").getData(1));
 
 	// two subject test:
 	File[] dataFileAry = {data1, data2}; // files to combine
 	subjectTestsHelper(dataFileAry, groupFile);
 
 	// three subject test:
 	File[] dataFileAry2 = {data1, data2, data3}; // files to combine
 	subjectTestsHelper(dataFileAry2, groupFile);
     }
     private static void subjectTestsHelper(File[] dataFileAry, File groupFile) {
 	// create ArrayList of the files:
 	ArrayList<File> dataFileLst = new ArrayList<File>(); 
 	for (File f : dataFileAry){
 	    dataFileLst.add(f);
 	}
 	
 	// Combine groups and display contents of combined group:
 	System.out.println("result of combining groups:");
 	printList(processAllSubjectData(dataFileLst,groupFile
 					).getGroup("foo").getData(1));	
     }
     private static void runANOVATests() {
 	System.out.println("Running ANOVA tests.");
 	
 	File groupFile = new File("c:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/ANOVA Testing/legitGroups");
 	File hbFile = new File("C:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/ANOVA Testing/legitHb");
 	int precision = 7;
 	int numChunks = 10;
 	
 	GroupedChannels groups1 = new GroupedChannels(hbFile, groupFile);
 
 	String groupName = "groupOne";
 	runANOVATestsHelper(groups1, groupName, 0); 
 	runANOVATestsHelper(groups1, groupName, 1); 
 
 	LinkedList<double[]> container = new LinkedList<double[]>();
 	container.add(toPrimitiveDoubleArray(groups1.getGroup(groupName).getData(0)));
 	container.add(toPrimitiveDoubleArray(groups1.getGroup(groupName).getData(1)));
 
 	OneWayAnova myANOVA = new OneWayAnova();
 	double result = myANOVA.anovaPValue(container);
 	System.out.println("Result: " + result);
 	
 	// outputANOVAs(groups1,
 	// 	     groups1.getGroupNames(), groups1.getConditions(),
 	// 	     numChunks, precision);
 	File outFile = new File("C:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/ANOVA Testing/outputFile.csv");
 	writeANOVAs(outFile, groups1,
 		    groups1.getGroupNames(), groups1.getConditions(),
 		    // groupNames, conditions,
 		    numChunks, precision);
 	System.out.println("Done.");
     }
     private static void runANOVATestsHelper(GroupedChannels groups,
 					    String groupName,
 					    int condition) {
 	System.out.println("(" + groupName + ", condition " + condition + ")");
 	printList(groups.getGroup(groupName).getData(condition)
 		  );
     }
     private static void runChannelsTests() {
 	System.out.println("Running channel grouping warnings tests.");
 	File hbFile = new File("C:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/channelWarningsTesting/legitHb");
 
 	// test 1:
 	File groupFile = new File("c:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/channelWarningsTesting/badGroupFile");
 	GroupedChannels foo = new GroupedChannels(hbFile, groupFile);
 
 	System.out.println("------------------------------------------");
 	
 	// test 2:
 	groupFile = new File("c:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/channelWarningsTesting/badGroupFile2");
 	foo = new GroupedChannels(hbFile, groupFile);
 	
 	System.out.println("Done.");	
     }
     public static void main(String[] args) {
 	//runChunkTests();
 	//runSubjectTests();
 	//runANOVATests();
 	runChannelsTests();
 	
         File legitGroupsFile = new File("c:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/legitGroups");
 	File legitHbFile = new File("C:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/legitHb");
 	int precision = 7;
 	int numChunks = 10;
 	GroupedChannels legitGroups = new GroupedChannels(legitHbFile,
 							  legitGroupsFile);
 	// ArrayList<String> mylst = legitGroups.getGroupNames();
 	// System.out.println(mylst.size());
 	// String foo = mylst.get(0);
 	// System.out.println(foo);
         // printList(legitGroups.getGroupNames());
 	// legitGroups.getGroup("groupOne");
 	// legitGroups.getGroup("secondGroup");
 	// legitGroups.getGroup("groupThree");
 	// legitGroups.getGroup("group4");
 	// legitGroups.getGroup("g5");
 	
 	/*
 	outputANOVAs(legitGroups,
 		     legitGroups.getGroupNames(), legitGroups.getConditions(),
 		     numChunks, precision);
 	File outFile = new File("C:/Users/nkolesar/Desktop/CS Seminar/fNIRs/sub19/outputFile.csv");
 	writeANOVAs(outFile, legitGroups,
 		    legitGroups.getGroupNames(), legitGroups.getConditions(),
 		    // groupNames, conditions,
 		    numChunks, precision);
 	*/
 	// group groupOne condition 0
 	// group groupOne condition 1
 	// --------------------------
 	// ==> ANOVA value of 0.0000...000 (50 decimal places)
 	//  WHY?
 
 	System.exit(0);
     }
 }
