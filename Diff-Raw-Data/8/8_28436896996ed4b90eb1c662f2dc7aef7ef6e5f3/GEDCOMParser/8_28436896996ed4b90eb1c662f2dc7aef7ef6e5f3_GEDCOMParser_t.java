 package rtay;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author Raymond Tay
  * @date 05 July 2012
  * @version 1.0
  * @brief GEDCOM xml parser console application for Aconex coding challenge
  */
 public class GEDCOMParser {
 	private static final String REGEX_GEDCOM_LINE = "(\\d+)(\\s+)(\\S+)(\\s*)(.*)";
 	private static final String REGEX_GEDCOM_ID_FROM_TAGORID = "(@)(.*)(@)";
 	private static final String MSG_PARSING = "Parsing GEDCOM file...";
 	private static final String MSG_WRITING_OUTPUT = "Generating XML file...";
 	private static final String MSG_CONVERT_COMPLETE = "Conversion successful...";
 	
 	/**
 	 * @brief Main program of GEDCOMParser
 	 * @param args Command-line arguments
 	 */
 	public static void main(String[] args) {
 		// validate expected arguments
 		if(args.length != 2) {
 			// filename not provided or invalid arguments
 			System.out.println("Syntax: GEDCOMParser <inputFile.txt> <outputFile.xml>");
 			System.exit(1);
 		}
 			
 		// setup filename aliases (for ease of change in args)
 		final String inputFilePath = args[0];
 		final String outputFilePath = args[1];
 				
 		try
 		{	
 			// parse GEDCOM file into XmlTag object	
 			System.out.println(MSG_PARSING + inputFilePath);
 			XmlTag data = parseGedcomFile(inputFilePath);
 			
 			// output objects into XML string			
 			System.out.println(MSG_WRITING_OUTPUT + outputFilePath);
 			writeToFile(outputFilePath, data.toString(), false);
 			
 			// display success message
 			System.out.println(MSG_CONVERT_COMPLETE);
 		}
 		catch (IOException err) {
 			// write java stack error message to console
 			System.out.println(err.getMessage());
 			
 			// Signify error with exit code (for possible interfacing with UI shell calls)
 			System.exit(2);
 		}	
 	}
 
 	/**
 	 * @brief	Method to parse GEDCOM encoded file into XmlTag object
 	 * @param filepath - path to GEDCOM file
 	 * @return XmlTag object containing XML
 	 * @throws IOException when file read error has occurred
 	 */
 	private static XmlTag parseGedcomFile(String filepath) throws IOException{
 		// init 
 		String lineRead;
 		Stack <XmlTag> tagStack = new Stack <XmlTag> ();
 		XmlTag mainTag = new XmlTag("gedcom");
 		mainTag.setLevel(0);
 		tagStack.push(mainTag);
 		
 		// init regex pattern for GEDCOM file
 		Pattern gedcomRegex = Pattern.compile(GEDCOMParser.REGEX_GEDCOM_LINE);
 				
 		// init file handles
 		BufferedReader br = new BufferedReader(new FileReader(filepath));
 		try {
 			// read file and parse data line by line
 			while ( (lineRead = br.readLine())!= null) {
 				// regex filtering 
 				Matcher regexMatcher = gedcomRegex.matcher(lineRead);
 				if(regexMatcher.find()) {
 					// acquire data from gedcom formatted line
 					int level = Integer.parseInt(regexMatcher.group(1)) + 1; // Add 1 level to account for <GEDCOM> main tag
 					String tagOrId = regexMatcher.group(3); // TAGORID
 					String data = regexMatcher.group(5); // DATA
 					
 					// pop tagStack until lastElement is always the parent tag for current line
 					while(level <= tagStack.lastElement().getLevel() ) {
 						tagStack.pop();
 					}				
 					
 					// create new XmlTag object from current file line
 					XmlTag xObj = XmlTag.createGEDCOMTag(tagOrId, data, GEDCOMParser.REGEX_GEDCOM_ID_FROM_TAGORID);
 							
 					// link newly created XmlTag object to parent Tag
 					tagStack.lastElement().addChildTag(xObj);
 					tagStack.push(xObj);
 				}
 			}
 		}
 		finally {
 			// always close file handle
 			br.close();
 		}
 		return mainTag;
 	}
 	
 	/**
 	 * @brief Writes a given string to a file
 	 * @param filepath	Absolute path to a line to write
 	 * @param stringToWrite	Data to be written to the file
 	 * @param newline	Boolean flag to create a new line at end of string
 	 * @throws IOException	When file writing error has occurred
 	 */
 	private static void writeToFile(String filepath, String stringToWrite, boolean newline) throws IOException{
 		BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
 		
 		try {
 			// buffered write string to file
 			bw.write(stringToWrite);
 			
 			// add newline if flag is set
 			if(newline) {
 				bw.newLine();
 			}
 		}
 		finally {
 			// always close handles
 			bw.close();
 		}
 	}
 }
