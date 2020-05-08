 import java.io.IOException;
 
 import java_cup.internal_error;
 
 public class AnalyzersGenerator {
 	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
 	
 	private static final String OUTPUT_DIR = "outputSrc";
 
 	private static final String FLEX_FILE_NAME = "files" + FILE_SEPARATOR
 			+ "lcalc.flex";
 
 	private static final String CUP_FILE_NAME = "files" + FILE_SEPARATOR
 			+ "ycalc.cup";
 
 	private static final String CUP_FILE_NAME_WITH_ACTIONS = "files"
 			+ FILE_SEPARATOR + "ocl_semantica.cup";
 
 	public static void generateLexicalAnalyzer() {
 		System.out.println("***************************************");
 		System.out.println("Generating lexical analyzer (JFlex) ...");
 		System.out.println("***************************************");
 		String[] args = { FLEX_FILE_NAME, "-d", OUTPUT_DIR };
 		JFlex.Main.main(args);
 		System.out.println();
 	}
 	
 	public static void generateSyntacticAnalyzer(boolean useSemanticActions) throws internal_error,
 			IOException, Exception {
 		generateSyntacticAnalyzer(false, useSemanticActions);
 	}
 
 	public static void generateSyntacticAnalyzer(boolean displaySummary,
 			boolean useSemanticActions)
 			throws internal_error, IOException, Exception {
 		System.out.println("***************************************");
 		System.out.println("Generating Syntatic Analyzer (CUP)");
 		System.out.println("***************************************");
 
 		String[] args;
 		String cupFile = useSemanticActions ? CUP_FILE_NAME_WITH_ACTIONS : CUP_FILE_NAME;
 		if (displaySummary) {
 			args = new String[] { "-parser",
					"SyntacticAnalyzer", "-destdir", OUTPUT_DIR,
 					"-interface",
 					cupFile };
 		} else {
 			args = new String[] { "-parser",
					"SyntacticAnalyzer", "-destdir", OUTPUT_DIR,
 					"-interface",
 					"-nosummary", cupFile };
 		}
 
 		java_cup.Main.main(args);
 		
 		if (!displaySummary) {
 			System.out.println("Success!");
 		}
 
 		System.out.println();
 	}
 
 	public static void main(String[] args) throws internal_error, IOException, Exception {
 		boolean useSemanticAtions = false; //args.length == 0 ? false : args[0].equals("-actions");
 		
 		generateLexicalAnalyzer();
 		generateSyntacticAnalyzer(useSemanticAtions);
 	}
 }
