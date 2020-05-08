 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Iterator;
 
 import plume.Option;
 import plume.OptionGroup;
 import plume.Options;
 
 import common.HistoryGraph;
 import common.Repository;
 import common.Revision;
 import common.Util;
 
 public class TestIsolationDataGenerator {
 
     /**
      * Print the short usage message.
      */
     @OptionGroup("General Options")
     @Option(value = "-h Print short usage message", aliases = { "-help" })
     public static boolean showHelp = false;
 
     /**
      * Full path to the serialized output file.
      */
    @Option(value = "-o File to use for serialized output.", aliases = { "-serializedOutputFile" })
     public static String serializedOutputFileName = null;
     
     /**
      * Full path to the human-readable output file.
      */
     @Option(value = "-o File to use for human-readable output.", aliases = { "-humanReadOutputFile" })
     public static String humanReadOutputFileName = null;
 
     /**
      * The commit ID from which to begin the analysis.
      */
     @Option(value = "-s Starting commit id")
     public static String startCommitID = null;
 
     /**
      * The commit ID where the analysis should terminate.
      */
     @Option(value = "-e Ending commit id")
     public static String endCommitID = null;
 
     /**
      * Full path to the repository directory.
      */
     @Option(value = "-r Full path to the repository directory.",
             aliases = { "-repodir" })
     public static String repositoryDirName = null;
 
     /** One line synopsis of usage */
     public static final String usage_string = "TestIsolationDataGenerator [options]";
     
     /** period of writing result to serialized file **/
     public static final int PERIOD = 10;
 
     /**
      * Initial program entrance -- parses the arguments and runs the data
      * extraction.
      * 
      * @param args : command line arguments.
      * @throws IOException
      */
     public static void main(String[] args) throws IOException {
        Options plumeOptions = new Options(
                TestIsolationDataGenerator.usage_string);
         plumeOptions.parse_or_usage(args);
 
         // Display the help screen.
         if (showHelp) {
             plumeOptions.print_usage();
             return;
         }
 
         HistoryGraph historyGraph = extractData();
         writeToSerializedFile(historyGraph);
         Util.writeToHumanReadableFile(humanReadOutputFileName, historyGraph);
     }
 
     /**
      * create a HistoryGraph instance from the given repository
      * @return HistoryGraph
      */
     public static HistoryGraph extractData() throws IOException {
     	Repository repository = new Repository(repositoryDirName);
     	HistoryGraph historyGraph = repository.buildHistoryGraph(startCommitID, endCommitID);
     	
     	if (historyGraph.iterator().hasNext()) {
     		Revision startRevision = historyGraph.iterator().next();
     		populateTestResults(historyGraph, startRevision);
     	}
     	
     	return historyGraph;
     }
     
     /**
      * construct a TestResult instance for each revision in the historyGraph, 
      * starting from startRevision
      * 
      * @modifies historyGraph
      * @throws IOException 
      * @throws FileNotFoundException 
      */
     public static void populateTestResults(HistoryGraph historyGraph, Revision startRevision) throws FileNotFoundException, IOException {
     	Iterator<Revision> itr = historyGraph.iterator();
     	
     	int count = 0;
     	Revision next = null;
     	while (itr.hasNext() && !(next = itr.next()).equals(startRevision)) { /* search for startRevision */ }
     	
     	if (next != null && next.equals(startRevision)) {
     		next.getTestResult();
     		count++;
     		
 	    	while (itr.hasNext()) {
 	    		Revision revision = itr.next();
 	    		revision.getTestResult();
 	    		count++;
 	    		
 	    		if (count % PERIOD == 0) {
 	    			writeToSerializedFile(historyGraph);
 	    		}
 	    	}
     	}
     }
     
 
     /**
      * write historyGraph to a serialized file
      */
     public static void writeToSerializedFile(HistoryGraph historyGraph) {
     	ObjectOutputStream output;
     	
     	try {
 			output = new ObjectOutputStream(new FileOutputStream(serializedOutputFileName));
 			output.writeObject(historyGraph);
 			output.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
     }
     
     /**
      * read historyGraph from a serialized file
      * 
      * @return historyGraph
      */
     public static HistoryGraph readFromSerializedFile() {
     	HistoryGraph hGraph = null;
     	ObjectInputStream input;
     	
     	try {
 			input = new ObjectInputStream(new FileInputStream(serializedOutputFileName));
 			hGraph = (HistoryGraph) input.readObject();
 			input.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		return hGraph;
     }
 }
