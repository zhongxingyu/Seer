 package uk.co.mtford.jalp;
 
 import org.apache.log4j.Logger;
 import uk.co.mtford.jalp.abduction.AbductiveFramework;
 import uk.co.mtford.jalp.abduction.DefinitionException;
 import uk.co.mtford.jalp.abduction.logic.instance.*;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.IConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.IEqualityInstance;
 import uk.co.mtford.jalp.abduction.parse.program.JALPParser;
 import uk.co.mtford.jalp.abduction.parse.program.ParseException;
 import uk.co.mtford.jalp.abduction.parse.program.TokenMgrError;
 import uk.co.mtford.jalp.abduction.parse.query.JALPQueryParser;
 import uk.co.mtford.jalp.abduction.rules.LeafRuleNode;
 import uk.co.mtford.jalp.abduction.rules.RuleNode;
 import uk.co.mtford.jalp.abduction.rules.visitor.FifoRuleNodeVisitor;
 import uk.co.mtford.jalp.abduction.rules.visitor.RuleNodeVisitor;
 
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Main entry point. Provides a terminal like interface.
  *
  * @author mtford
  */
 public class Main {
 
     private static final Logger LOGGER = Logger.getLogger(Main.class);
     private static final Scanner sc = new Scanner(System.in);
 
    private static final int MAX_EXPANSIONS = 10000;
 
     // Command line options.
     private static final String FILE_OPTION = "-f";
     private static final String HELP_OPTION = "-h";
     private static final String QUERY_OPTION = "-q";
     private static final String DEBUG_OPTION = "-d";
     private static final String XML_OPTION = "-x";
     private static final String JSON_OPTION = "-j";
 
     // ALPS commands.
     private static final char FILE_COMMAND = 'l';
     private static final char QUERY_COMMAND = 'q';
     private static final char CLEAR_COMMAND = 'c';
     private static final char HELP_COMMAND = 'h';
     private static final char DUMP_COMMAND = 'm';
     private static final char DEBUG_COMMAND = 'd';
 
     // Messages.
     private static final String EXEC_HELP
             = "Options:" + "\n" +
             "-h            : This help." + "\n" +
             "-f <file-path>: Read from file." + "\n" +
             "-q <query>    : Execute a query on the given file" + "\n" +
             "-d            : Enables debug mode ";
     private static final String EXEC_ARG_ERROR
             = "Problem with arguments. Use -h option for help.";
     private static final String JALP_HELP
             = "Enter facts, rules and integrity constraints or use the following options." + "\n" + "\n" +
             "Options" + "\n" +
             "-------" + "\n" +
             "Load a file    => :l (<filename>)+" + "\n" +
             "Run a query    => :q (p(u1,...,un),)* p(u1,...,un)" + "\n" +
             "Reset system   => :c" + "\n" +
             "Memory dump    => :m" + "\n" +
             "Debug mode     => :d" + "\n" +
             "This help      => :h";
     private static final String UNKNOWN_COMMAND
             = "Command not recognised. Use :h for help.";
     private static final String INVALID_USAGE
             = "Invalid usage. Use :h help.";
 
     private static boolean debugMode = false;
     private static boolean xmlMode = false;
     private static boolean jsonMode = false;
     private static String xmlFileName = "../visualizer/output.xml";
     private static String jsFileName = "../visualizer/output.js";
     private static AbductiveFramework framework;
 
     /**
      * Prints a message to standard error and then quits with error status.
      *
      * @param error
      */
     private static void fatalError(String error) {
         System.err.println(error);
         System.exit(-1);
     }
 
     /**
      * Prints a message to standard out.
      *
      * @param str
      */
     private static void printMessage(String str) {
         System.out.println(str);
     }
 
     /**
      * Prints a message to standard out along with the string representation of some throwable.
      *
      * @param str
      * @param t
      */
     private static void printMessage(String str, Throwable t) {
         System.out.println(str);
         t.printStackTrace(System.out);
     }
 
     private static void mergeFramework(AbductiveFramework newFramework) {
         if (framework == null) {
             framework = newFramework;
         } else {
             framework.getP().addAll(newFramework.getP());
             framework.getIC().addAll(newFramework.getIC());
             framework.getA().putAll(newFramework.getA());
         }
     }
 
     /**
      * Resets the abductive framework.
      */
     private static void reset() {
         framework = new AbductiveFramework();
     }
 
     /**
      * Loads and parses an abductive logic program and constructs the framework object.
      *
      * @param nextLine
      */
     private static void loadFiles(String nextLine) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Loading file called " + nextLine);
         String[] fileNames = nextLine.split(" ");
         if (fileNames.length < 2) {
             printMessage(INVALID_USAGE);
         } else {
             for (int i = 1; i < fileNames.length; i++) {
                 AbductiveFramework newF = null;
                 try {
                     newF = JALPParser.readFromFile(fileNames[i]);
                 } catch (FileNotFoundException ex) {
                     printMessage("No such file.", ex);
                 } catch (ParseException ex) {
                     printMessage("Parse error in file " + fileNames[i], ex);
                 } catch (TokenMgrError ex) {
                     printMessage("Token error\n" + ex.getMessage(), ex);
                 }
                 if (newF != null) mergeFramework(newF);
             }
         }
     }
 
     /**
      * Parses and executes a query on the currently held abductive framework. Operates in either debug (step) mode
      * or performs all steps and returns the results.
      *
      * @param query
      * @throws uk.co.mtford.jalp.abduction.parse.query.ParseException
      *
      */
     private static void processQuery(String query) throws uk.co.mtford.jalp.abduction.parse.query.ParseException, DefinitionException, IOException {
         List<PredicateInstance> predicates = JALPQueryParser.readFromString(query);
         List<IInferableInstance> goals = new LinkedList<IInferableInstance>();
         Set<VariableInstance> queryVariables = new HashSet<VariableInstance>();
         goals.addAll(predicates);
         for (IInferableInstance inferable:goals) {
             queryVariables.addAll(inferable.getVariables());
         }
         IInferableInstance firstGoal = goals.remove(0);
         RuleNode currentNode = firstGoal.getPositiveRootRuleNode(framework,goals);
         RuleNode rootNode = currentNode;
         currentNode.getNextGoals().addAll(framework.getIC());
         RuleNodeVisitor visitor = new FifoRuleNodeVisitor(currentNode);
         if (debugMode) {
             printMessage("Framework is as follows:\n\n"+framework+"\n");
             printMessage("Query is as follows:\n\n"+predicates+"\n");
             printMessage("Initializing derivation tree...");
 
             do {
                 printMessage("\nCurrent state for query " + predicates + " is");
                 printMessage("==============================================================");
                 printMessage(currentNode.toString());
                 printMessage("==============================================================");
                 printMessage("Enter c to continue or anything else to quit.");
                 String s = sc.nextLine();
                 if (s.trim().equals("c")||s.trim().equals("cc")||s.trim().equals("ccc")) {
                     currentNode=visitor.stateRewrite();
                     continue;
                 }
                 else {
                     break;
                 }
             } while (currentNode!=null);
         }
         else if (xmlMode) {
             try {
                 int n = 0;
                 if (LOGGER.isInfoEnabled()) LOGGER.info("Beginning processing of query.");
                 do {
                     if (n>=MAX_EXPANSIONS) {
                         LOGGER.error("Hit max expansions. Generating XML file for what we have so far.");
                     }
                     currentNode=visitor.stateRewrite();
                 } while (currentNode!=null);
             } catch (Exception e) {
                 LOGGER.fatal("Error in tree generation whilst making XML file. Saving tree so far to "+xmlFileName,e);
             }
             printXML(xmlFileName,rootNode);
         }
         else if (jsonMode) {
             try {
                 int n = 0;
                 if (LOGGER.isInfoEnabled()) LOGGER.info("Beginning processing of query.");
                 do {
                     n++;
                     if (n>=MAX_EXPANSIONS) {
                         LOGGER.error("Hit max expansions. Generating JSON file for what we have so far.");
                     }
                     currentNode=visitor.stateRewrite();
                 } while (currentNode!=null);
             } catch (Exception e) {
                 LOGGER.fatal("Error in tree generation whilst making JSON file. Saving tree so far to "+jsFileName,e);
             }
             printJSON(jsFileName,rootNode);
         }
         else {
             if (LOGGER.isInfoEnabled()) LOGGER.info("Beginning processing of query.");
             do {
                 currentNode=visitor.stateRewrite();
 
             } while (currentNode!=null);
             printMessage("Found " + visitor.getLeafRuleNodes().size() + " explanations for query "+predicates);
             List<LeafRuleNode> successNodes = visitor.getLeafRuleNodes();
             printResults(queryVariables, successNodes);
         }
         printMessage("Exiting...");
 
 
     }
 
     private static void printResults(Set<VariableInstance> queryVariables, List<LeafRuleNode> leafRuleNodes) {
         for (int i=0;i< leafRuleNodes.size();i++) {
             printMessage("Enter c to see next explanation or anything else to quit.");
             String s = sc.nextLine();
             if (s.trim().equals("c")||s.trim().equals("cc")||s.trim().equals("ccc")) {
                 Set<VariableInstance> variables = new HashSet<VariableInstance>(queryVariables);
                 Map<VariableInstance,IUnifiableAtomInstance> assignments = leafRuleNodes.get(i).getAssignments();
                 List<PredicateInstance> abducibles = leafRuleNodes.get(i).getStore().abducibles;
                 List<DenialInstance> denials = leafRuleNodes.get(i).getStore().denials;
                 List<IEqualityInstance> equalities = leafRuleNodes.get(i).getStore().equalities;
                 List<IConstraintInstance> constraints = leafRuleNodes.get(i).getStore().constraints;
                 for (PredicateInstance predicate:abducibles) {
                     variables.addAll(predicate.getVariables());
                 }
                 for (DenialInstance denial:denials) {
                     variables.addAll(denial.getVariables());
                 }
                 printMessage("==============================================================");
                 printMessage("Abducibles: "+abducibles);
                 printMessage("Assignments: "+assignments);
                 printMessage("Denials: "+denials);
                 printMessage("Equalities: "+equalities);
                 printMessage("Finite-Domain Constraints: "+constraints);
                 printMessage("==============================================================");
                 printMessage("There are "+(leafRuleNodes.size()-1-i)+" explanations remaining.");
                 continue;
             }
             else {
                 break;
             }
         }
     }
 
     private static void printXML(String fileName, RuleNode root) throws IOException {
         FileWriter fstream = new FileWriter(fileName);
         BufferedWriter out = new BufferedWriter(fstream);
         String xml="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";
         //xml+="<?xml-stylesheet type=\"text/css\" href=\"css/style.css\"?>\n";
         xml+="<tree>\n";
         xml+=root.toXML();
         xml+="</tree>";
         out.write(xml);
         out.close();
     }
 
     private static void printJSON(String fileName, RuleNode root) throws IOException {
         FileWriter fstream = new FileWriter(fileName);
         BufferedWriter out = new BufferedWriter(fstream);
         String js = "var data=\""+root.toJSON()+"\"";
         out.write(js);
         out.close();
     }
 
     private static void startJALP() {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Starting JALP.");
         while (true) {
             System.out.print("JALP -> ");
             String nextLine = sc.nextLine();
             char c = ' ';
             if (!nextLine.isEmpty()) c = nextLine.charAt(0);
             if (c == ':') { // Execute a command.
                 c = nextLine.charAt(1);
                 switch (c) {
                     case FILE_COMMAND:
                         loadFiles(nextLine);
                         break;
                     case QUERY_COMMAND:
                         try {
                             processQuery(nextLine.substring(2, nextLine.length()));
                         } catch (uk.co.mtford.jalp.abduction.parse.query.ParseException ex) {
                             printMessage("Invalid query\n" + ex.getMessage());
                         } catch (uk.co.mtford.jalp.abduction.parse.query.TokenMgrError ex) {
                             printMessage("Invalid query\n" + ex.getMessage());
                         } catch (Exception e) { // Process query doesn't yet work. So avoid a crash.
                             LOGGER.error("JALP has encountered a problem.", e);
                         }
                         break;
                     case CLEAR_COMMAND:
                         reset();
                         System.out.println("All cleared.");
                         break;
                     case HELP_COMMAND:
                         printMessage(JALP_HELP);
                         break;
                     case DUMP_COMMAND:
                         printMessage(framework.toString());
                         break;
                     case DEBUG_COMMAND:
                         debugMode = !debugMode;
                         printMessage(debugMode ? "Debug mode enabled." : "Debug mode disabled.");
                         break;
                     default:
                         printMessage(UNKNOWN_COMMAND);
                 }
             } else if (c == ' ') { // Carry on.
                 continue;
             } else { //
                 AbductiveFramework newF = null;
                 try {
                     newF = JALPParser.readFromString(nextLine);
                 } catch (ParseException ex) {
                     printMessage("Parse error\n" + ex.getMessage());
                 } catch (TokenMgrError ex) {
                     printMessage("Invalid syntax\n" + ex.getMessage());
                 }
                 if (newF != null) mergeFramework(newF);
             }
         }
     }
 
     /**
      * Main point of entry. Either starts up the JALP command line if query not provided via program arguments
      * or performs the derivation and returns the result.
      *
      * @param args
      * @throws uk.co.mtford.jalp.abduction.parse.query.ParseException
      *
      */
     public static void main(String[] args) throws uk.co.mtford.jalp.abduction.parse.query.ParseException, DefinitionException, IOException {
         boolean console = false;
         boolean file = false;
         boolean query = false;
         String fileName = null;
         String queryString = null;
         AbductiveFramework f = new AbductiveFramework();
 
         for (int i = 0; i < args.length; i++) {
             String arg = args[i];
 
             if (arg.equals(FILE_OPTION)) {
                 if (console) {
                     fatalError("Can choose either console or file.");
                 }
                 if (file) {
                     fatalError("Already specified a file.");
                 }
                 if (jsonMode || xmlMode) {
                     fatalError("JSON mode and XML mode are not available during debug.");
                 }
                 file = true;
                 i++;
                 fileName = args[i];
             } else if (arg.equals(HELP_OPTION)) {
                 printMessage(EXEC_HELP);
                 System.exit(0);
             } else if (arg.equals(QUERY_OPTION)) {
                 query = true;
                 i++;
                 queryString = args[i];
             } else if (arg.equals(DEBUG_OPTION)) {
                 debugMode = true;
 
             } else if (arg.equals(XML_OPTION)) {
                 if (jsonMode) {
                     fatalError("Can choose either XML mode or JSON mode.");
                 }
                 else if (debugMode) {
                     fatalError("XML mode not available during debug.");
                 }
                 xmlMode = true;
             } else if (arg.equals(JSON_OPTION)) {
                 if (xmlMode) {
                     fatalError("Can choose either JSON mode or XML node.");
                 }
                 else if (debugMode) {
                     fatalError("JSON mode not available during debug.");
                 }
                 jsonMode = true;
             }
             else {
                 printMessage(EXEC_ARG_ERROR);
             }
         }
 
         if (file) {
             System.out.println("Reading from " + fileName);
 
             try {
                 f = JALPParser.readFromFile(fileName);
                 System.out.println("Successfully read " + fileName);
             } catch (FileNotFoundException ex) {
                 LOGGER.error("Cannot find file.");
                 file = false;
             } catch (ParseException ex) {
                 LOGGER.error("Syntax error in " + fileName, ex);
                 file = false;
             }
 
         }
 
         if (query && file) {
             mergeFramework(f);
             processQuery(queryString);
         } else if (!query) {
             mergeFramework(f);
             startJALP();
         }
 
     }
 
 }
