 package com.ntnu.tdt4215;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.lucene.queryparser.classic.ParseException;
 
 import com.ntnu.tdt4215.document.ScoredDocument;
 import com.ntnu.tdt4215.searchEngine.SearchEngine;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.nio.charset.Charset;
 import java.util.Collection;
 import java.util.Iterator;
 
 public class App {
 	
 	private static final int MAXDOCS = 5;
 	static SearchEngine manager;
 	static PrintStream stdout;
 	
 	public static void main(String[] args) throws ParseException, IOException {
 		stdout = new PrintStream(System.out, true, "UTF-8");
 		System.setOut(stdout);
 		manager = new com.ntnu.tdt4215.searchEngine.NLHIcd10();
 		//manager = new com.ntnu.tdt4215.searchEngine.NLH();
 		
 		// We clean the folder containing the index
     	if (args.length == 1 && args[0].equals("--clean")) {
     		try {
 				manager.clean();
 			} catch (IOException e) {
 				System.err.println(e.getMessage());
 				System.exit(-1);
 			}
     	} else if (args.length == 1 && args[0].equals("--index")) {
     		// We want to index all accessible documents
     		try {
 				manager.indexAll();
 			} catch (IOException e) {
 				System.err.println("An error occured while indexing");
 				System.exit(-2);
 			}
     	} else if(args.length == 1 && args[0].equals("--search")) {
     		// We want to search the index
     		searchLoop();
     	} else {
     		// Invalid entry we show the help
     		showHelp();
     	}
     }
 	
 	/**
 	 * Creates a loop that will ask for the user to enter a filename to send 
 	 * @throws IOException
 	 * @throws ParseException
 	 */
 	private static void searchLoop() throws IOException, ParseException {
 		boolean stop = false;
 		java.io.BufferedReader stdin = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
 		while (!stop) {
 			System.out.println("Please insert the filename that contains "+ 
 							   "the patient case to quit the program type twice return");
 			String line = stdin.readLine();
 			if (line == null || line.equals("")) {
 				stop = true;
 			} else {
 				System.out.println("Searching the content of file:"+ line);
 				searchFile(line);
 			}
 		}
 		System.out.println("Quitting");	
 	}
 
 	/**
 	 * Show the help message
 	 */
 	private static void showHelp() {
 		System.out.println("-------TDT 4215 project app usage-----");
 		System.out.println("By: Anne-Sophie Gourlay, David Katuscak and Charly Molter");
 		System.out.println("\tOptions:");
 		System.out.println("\t\t--index: index documents");
 		System.out.println("\t\t--search start the program to search the index");
 		System.out.println("\t\t--clean: empty the index");
 	}
 	
 	/**
 	 * Search the index with as a query the content of the file line
 	 * @param line
 	 * @throws IOException
 	 * @throws ParseException
 	 */
 	private static void searchFile(String line) throws IOException, ParseException {
 		File f = new File(line);
 		if (!f.exists() || !f.canRead()) {
 			System.err.println("Can't read the file: " + line);
 		} else {
     		String contents = FileUtils.readFileToString(f, Charset.forName("UTF-8"));
     		Collection<ScoredDocument> docs = manager.getResults(MAXDOCS, contents);
     		System.out.println("Matches:");
     		Iterator<ScoredDocument> it = docs.iterator();
     		int i = 0;
     		while (it.hasNext() && i < MAXDOCS) {
    			System.out.println(it.next().getField("title"));
     		}
 		}
 	}
 
 
 }
