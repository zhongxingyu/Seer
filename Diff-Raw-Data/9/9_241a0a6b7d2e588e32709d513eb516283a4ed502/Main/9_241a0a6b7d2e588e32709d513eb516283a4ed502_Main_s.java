 package Source;
 
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 // Use java -jar <this>
 public class Main {
 	private static char EOF = (char)65535;
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		// Parameters passed in
 		if (args.length < 2) {
 			System.out.println("USAGE: ScannerGenerator <SPECIFICIATION_FILE> <INPUT_FILE> [<OUTPUT_FILE>]");
 			return;
 		}
 		String specificationFilename = args[0];
 		String inputFilename = args[1];
 		String outputFilename = args.length == 3 ? args[2] : inputFilename+"_Output.txt";
 		System.out.println("Output filename set to '"+outputFilename+"'");
 		
 		// Generate DFA table from regex specification file
 		System.out.println("Generating DFA Table for specification file '"+specificationFilename+"'...");
 		DFATable dfaTable = ScannerGenerator.generateDFA(specificationFilename);
 		System.out.println("Done generating DFA Table.");
 
 		// Build scanner using DFA table on input file
 		System.out.println("Initializing TableWalker with DFA Table...");
 		TableWalker tableWalker = new TableWalker(dfaTable);
 		System.out.println("Done initializing TableWalker.");
 
 		// Main driver
 		System.out.println("Walking Table with input file '"+inputFilename+"'...");
 		FileInputStream in = new FileInputStream(inputFilename);
 		char c;
 		ArrayList<Token> token;
 		ArrayList<String> tokenStringList = new ArrayList<String>();
 		while (  (c = (char)in.read()) != EOF  ) {
 			token = tableWalker.walkTable(c);
 			if (token != null) {
				Iterator tokenIterator = token.iterator();
 				while(tokenIterator.hasNext()){
 				String tokenString = "TOKEN!: "+tokenIterator.next();
 				System.out.println(tokenString);
 				tokenStringList.add(tokenString);
 				}
 			}
 		}
 		in.close();
 		System.out.println("Finished Walking Table! Found "+tokenStringList.size()+" tokens.");
 		
 		// Write tokens to output file , writing tokens to '"+outputFilename+"'
 		System.out.println("Writing tokens to output file '"+outputFilename+"'...");
 		BufferedWriter out = new BufferedWriter( new FileWriter(outputFilename) );
 		for (String tokenString : tokenStringList) out.write(tokenString);
 		out.close();
 		System.out.println("Finished writing tokens! All Done.");
 		
 	}
 	
 //	/**
 //	 * Creates output file from a run QueryEngine instance
 //	 * @param s
 //	 * @param outfilename String filename including ending (ex. 'out-small.txt')
 //	 */
 //	public static void writeOutput(QueryEngine q, String outputFilename) {		
 //		try {
 //			// Create file
 //			FileWriter fstream = new FileWriter(outputFilename);
 //			BufferedWriter out = new BufferedWriter(fstream);
 //			out.write("input document: "+q.s.getFilename()+"\n");
 //			
 //			out.write("\n1. Most frequent string: '"+q.getTop()+"'");
 //			out.write("\n2. Top 17 Most frequent strings: "+q.getTopK(17));
 //			out.write("\n3. Number of Tokens");
 //			out.write("\n     VARs="+q.getVarCount());
 //			out.write("\n     INTs="+q.getIntCount());
 //			out.write("\n   FLOATs="+q.getFloatCount());
 //			if (q.s.getInts().size() > 0) // Safety check, not related to q-engine
 //				out.write("\n4. [min, max] of INT  : ["+q.minInt()+","+q.maxInt()+"]");
 //			if (q.s.getFloats().size() > 0)
 //				out.write("\n5. [min, max] of FLOAT: ["+q.minFloat()+","+q.maxFloat()+"]");
 ////			out.write("\n6. All quotes:" + q.getQuotes());
 //			out.write("\n6. All "+q.getQuotes().size()+" quotes :");
 //			for (String quote : q.getQuotes())
 //				out.write("\n     " + quote);
 //			out.write("\n7. Number of VAR+(INT OR FLOAT) token occurrences: " + q.getNumVarNums()+"\n");
 //			
 //			// VAR
 //			out.write("\nVAR(" + q.getVarCount() + ") :\n");
 //			for (Entry<Object, Integer> e : q.getSortedVars())
 //				out.write(e.getKey() + ", " + e.getValue() + "\n");
 //
 //			// INT
 //			out.write("\nINT(" + q.getIntCount() + ") :\n");
 //			for (Entry<Object, Integer> e : q.s.getInts().entrySet())
 //				out.write(e.getKey() + ", " + e.getValue() + "\n");
 //
 //			// FLOAT
 //			out.write("\nFLOAT(" + q.getFloatCount() + ") :\n");
 //			for (Entry<Object, Integer> e : q.s.getFloats().entrySet())
 //				out.write(e.getKey() + ", " + e.getValue() + "\n");
 //			
 //			// Close the output stream
 //			out.close();
 //			System.out.println("All Output written to '"+outputFilename+"'");
 //		} catch (Exception e) {// Catch exception if any
 //			System.err.println("Error: " + e.getMessage());
 //		}
 //	}
 }
