 package histaroach;
 
 import histaroach.algorithm.IntermediateRevisionAnalysis;
 import histaroach.algorithm.IntermediateRevisionGenerator;
 import histaroach.buildstrategy.IBuildStrategy;
 import histaroach.buildstrategy.JodatimeBuildStrateygy;
 import histaroach.buildstrategy.VoldemortBuildStrategy;
 import histaroach.model.Flip;
 import histaroach.model.GitRepository;
 import histaroach.model.HistoryGraph;
 import histaroach.model.IRepository;
 import histaroach.model.IntermediateRevision;
 import histaroach.util.HistoryGraphXMLReader;
 import histaroach.util.HistoryGraphXMLWriter;
 import histaroach.util.IntermediateRevisionXMLReader;
 import histaroach.util.IntermediateRevisionXMLWriter;
 import histaroach.util.Util;
 import histaroach.util.XMLReader;
 import histaroach.util.XMLWriter;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import plume.Option;
 import plume.OptionGroup;
 import plume.Options;
 
 /**
  * DataCollector extracts information (HistoryGraph and IntermediateRevision) 
  * from the project subjects of study.
  */
 public class DataCollector {
 	
 	public static final String DATA_PATH = "data";
 	
 	// Prefix of files to which HistoryGraph instances are written.
     public static final String HISTORYGRAPH_PREFIX = "historyGraph";
     
     // Prefix of files to which IntermediateRevision instances are written.
     public static final String INTERMEDIATE_REVISION_PREFIX = "intermediateRevision";
     
     public static final String XML_EXTENSION = ".xml";
     public static final String TXT_EXTENSION = ".txt";
     
     // project subjects of study
     public static final String VOLDEMORT = "voldemort";
     public static final String JODA_TIME = "joda-time";
     
     /**
      * Print a help message.
      */
     @OptionGroup("General Options")
     @Option(value="-h Print a help message", aliases={"-help"})
     public static boolean help;
     
     /**
      * Collect HistoryGraph data.
      */
     @OptionGroup("Mode Options")
     @Option(value="Collect HistoryGraph data")
     public static boolean phaseI;
     
     /**
      * Create IntermediateRevisions.
      */
     @Option(value="Create IntermediateRevisions")
     public static boolean phaseII;
     
     /**
      * Run tests on IntermediateRevisions.
      */
     @Option(value="Run tests on IntermediateRevisions")
     public static boolean phaseIII;
     
     /**
 	 * Project name.
 	 */
     @OptionGroup("Common Options")
 	@Option(value = "-p Project name")
 	public static String projectName = null;
 	
 	/**
 	 * Repository directory.
 	 */
 	@Option(value = "-r <filename> Repository directory")
 	public static File repoDir = null;
 	
 	/**
      * Build command. Default is 'ant'.
      */
     @Option(value = "-b Build command (Optional)")
     public static String buildCommand = "ant";
     
     /**
      * The commit ID where HistoryGraph analysis begins.
      */
     @OptionGroup("HistoryGraph Options")
     @Option(value = "-s Starting commit ID for HistoryGraph analysis")
     public static String startCommitID = null;
     
     /**
      * The commit ID where HistoryGraph analysis ends.
      */
     @Option(value = "-e Ending commit ID for HistoryGraph analysis")
     public static String endCommitID = null;
     
     /**
 	 * Cloned repository directory.
 	 */
     @OptionGroup("IntermediateRevision Options")
 	@Option(value = "-c <filename> Cloned repository directory")
 	public static File clonedRepoDir = null;
     
     /**
      * HistoryGraph xml file.
      */
     @Option(value = "-H <filename> HistoryGraph xml file")
     public static File historyGraphXML = null;
     
     /**
      * IntermediateRevision xml file.
      */
     @OptionGroup("IntermediateRevision (run tests) Options")
     @Option(value = "-I <filename> IntermediateRevision xml file")
     public static File intermediateRevisionXML = null;
     
     /**
      * The index of IntermediateRevision to begin analysis.
      */
     @Option(value = "-i Index of IntermediateRevision to begin analysis (Optional)")
     public static int startIndex = 0;
     
     /**
      * The number of IntermediateRevisions to analyze.
      */
     @Option(value = "-n Number of IntermediateRevisions to analyze (Optional)")
     public static int numIntermediateRevisions = 0;
 
 	/** One line synopsis of usage */
 	public static final String usage_string = "DataCollector [mode option] [common options]"
 		+ " [HistoryGraph/IntermediateRevision options]";
     
 
 	/**
      * Initial program entrance -- parses the arguments and runs the specific 
      * type of data extraction.
      * 
      * @param args - command line arguments.
      * @throws Exception 
      */
 	public static void main(String[] args) throws Exception {
 		Options plumeOptions = new Options(usage_string, DataCollector.class);
 	    plumeOptions.parse_or_usage(args);
 	    
 	    // Display the help screen.
 	    if (help) {
 	        plumeOptions.print_usage();
 	        return;
 	    }
 	    
 	    if (projectName == null || repoDir == null) {
             plumeOptions.print_usage();
             return;
         }
 	    
 	    if (phaseI) {
 	    	if (startCommitID == null || endCommitID == null) {
 	            plumeOptions.print_usage();
 	            return;
 	        }
 	        	        
 	        IBuildStrategy buildStrategy;
 	        
 	        if (projectName.equals(VOLDEMORT)) {
 	        	buildStrategy = new VoldemortBuildStrategy(repoDir, buildCommand);
 	        } else if (projectName.equals(JODA_TIME)) {
 	        	buildStrategy = new JodatimeBuildStrateygy(repoDir, buildCommand);
 	        } else {
 	        	plumeOptions.print_usage("projectName must be either " 
 	        			+ VOLDEMORT + " or " + JODA_TIME);
 	            return;
 	        }
 	        
 	        IRepository repository = new GitRepository(repoDir, buildStrategy);
 	        
 	        String timeStamp = Util.getCurrentTimeStamp();
 	        HistoryGraph historyGraph = repository.buildHistoryGraph(startCommitID, endCommitID);
 	        
 	        saveHistoryGraph(historyGraph, timeStamp);
 	    	
 	    } else if (phaseII || phaseIII) {
 	    	if (clonedRepoDir == null || historyGraphXML == null) {
 		    	plumeOptions.print_usage();
 		    	return;
 		    }
 	    	
 	    	IBuildStrategy buildStrategy;
 	        IBuildStrategy clonedBuildStrategy;
 	        
 	        if (projectName.equals(VOLDEMORT)) {
 	        	buildStrategy = new VoldemortBuildStrategy(repoDir, buildCommand);
 	        	clonedBuildStrategy = new VoldemortBuildStrategy(clonedRepoDir, buildCommand);
 	        } else if (projectName.equals(JODA_TIME)) {
 	        	buildStrategy = new JodatimeBuildStrateygy(repoDir, buildCommand);
 	        	clonedBuildStrategy = new JodatimeBuildStrateygy(clonedRepoDir, buildCommand);
 	        } else {
 	        	plumeOptions.print_usage("projectName must be either " 
 	        			+ VOLDEMORT + " or " + JODA_TIME);
 	            return;
 	        }
 	        
 	        IRepository repository = new GitRepository(repoDir, buildStrategy);
 	        IRepository clonedRepository = new GitRepository(clonedRepoDir, clonedBuildStrategy);
 	        
 	        XMLReader<HistoryGraph> reader = new HistoryGraphXMLReader(historyGraphXML);
 		    HistoryGraph historyGraph = reader.read();
 	    	
 		    if (phaseII) {
 		    	createIntermediateRevisions(historyGraph, repository, clonedRepository);
 	        } else {
 	        	if (intermediateRevisionXML == null) {
 			    	plumeOptions.print_usage();
 			    	return;
 			    }
 	        	
 	        	runTestOnIntermediateRevisions(historyGraph, repository, clonedRepository);
 	        }
 	    } else {
 	    	plumeOptions.print_usage();
 	    }
 	}
 
 	/**
      * Writes historyGraph to an xml file.
      * 
      * @throws ParserConfigurationException 
      * @throws TransformerException 
      */
     public static void saveHistoryGraph(HistoryGraph historyGraph, String timeStamp) 
     		throws ParserConfigurationException, TransformerException {
     	String fileName = HISTORYGRAPH_PREFIX + "_" + startCommitID + "-" + endCommitID 
     			+ "_" + timeStamp + XML_EXTENSION;
     	File dir = new File(DATA_PATH + File.separatorChar + startCommitID 
     			+ "-" + endCommitID);
    	
    	if (!dir.exists()) {
    		dir.mkdir();
    	}
    	
     	File xmlFile = new File(dir, fileName);
     	
     	XMLWriter writer = new HistoryGraphXMLWriter(xmlFile, historyGraph);
     	writer.buildDocument();
     }
     
     /**
 	 * Creates a list of IntermediateRevisions from all flips in historyGraph, 
 	 * and writes them to an xml file.
 	 * 
 	 * @throws ParserConfigurationException
 	 * @throws TransformerException
      * @throws InterruptedException 
      * @throws IOException 
 	 */
 	public static void createIntermediateRevisions(HistoryGraph historyGraph, 
 			IRepository repository, IRepository clonedRepository) 
 			throws ParserConfigurationException, TransformerException, 
 			IOException, InterruptedException {
 		Set<Flip> flips = historyGraph.getAllFlips();
     	
     	IntermediateRevisionGenerator generator = new IntermediateRevisionGenerator(
     			repository, clonedRepository);
     	List<IntermediateRevision> intermediateRevisions = 
     		generator.generateIntermediateRevisionsFromFlips(flips);
     	
     	String filename = historyGraphXML.getName().replaceFirst(
     			HISTORYGRAPH_PREFIX, INTERMEDIATE_REVISION_PREFIX);
     	File xmlFile = new File(historyGraphXML.getParentFile(), filename);
     	
     	XMLWriter writer = new IntermediateRevisionXMLWriter(xmlFile, intermediateRevisions);
     	writer.buildDocument();
 	}
 	
 	/**
 	 * For a specified range of IntermediateRevisions, creates actual intermediate 
 	 * revisions on the file system, runs tests on them and records the results to 
 	 * an output file.
 	 * 
 	 * @throws Exception
 	 */
 	public static void runTestOnIntermediateRevisions(HistoryGraph historyGraph, 
 			IRepository repository, IRepository clonedRepository) 
 			throws Exception {
 		XMLReader<List<IntermediateRevision>> reader = new IntermediateRevisionXMLReader(
     			intermediateRevisionXML, repository, clonedRepository, historyGraph);
     	List<IntermediateRevision> intermediateRevisions = reader.read();
     	IntermediateRevisionAnalysis analysis = new IntermediateRevisionAnalysis(
     			intermediateRevisions);
     	
     	String xmlFilename = intermediateRevisionXML.getName();
     	String filename;
     	
     	if (numIntermediateRevisions > 0) {
     		String suffix = "_" + startIndex + "-" + (startIndex + 
     				numIntermediateRevisions) + TXT_EXTENSION;
     		filename = xmlFilename.replaceFirst(XML_EXTENSION, suffix);
     	} else {
     		filename = xmlFilename.replaceFirst(XML_EXTENSION, TXT_EXTENSION);
     	}
     	
     	File txtFile = new File(intermediateRevisionXML.getParentFile(), filename);
     	
     	if (numIntermediateRevisions > 0) {
 	    	analysis.runTestOnIntermediateRevisions(startIndex, numIntermediateRevisions, 
 	    			txtFile);
     	} else {
     		analysis.runTestOnIntermediateRevisions(txtFile);
     	}
 	}
 }
