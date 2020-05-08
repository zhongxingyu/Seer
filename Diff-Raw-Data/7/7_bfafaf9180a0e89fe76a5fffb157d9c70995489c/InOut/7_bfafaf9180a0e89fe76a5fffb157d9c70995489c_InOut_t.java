 package machine.turing;
 import java.io.*;
 import java.lang.String;
 import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /** This class provides methods to read and write Turing machines from / to files
  * 
  * @author David Wille
  * 
  */
 
 public class InOut {
 	/**
 	 * Writes a Turing machine to a LaTex file
 	 * @param fileName Name of the file, where to write to
 	 * @param machine Turing machine that should be written to the file
 	 */
 	public static void writeLatexToFile(String fileName, TuringMachine machine) {
 		// check for right file ending
 		if (!fileName.endsWith(".tex")) {
 			fileName = fileName + ".tex";
 		}
 
 		try {
 			// copy from template to new file
 			File inputFile = new File("template.tex");
 			File outputFile = new File(fileName);
 
 			FileReader in = new FileReader(inputFile);
 			FileWriter out = new FileWriter(outputFile);
 			int c;
 
 			while ((c = in.read()) != -1) {
 				out.write(c);
 			}
 			in.close();
 			out.close();
 
 			// edit new file
 			BufferedReader reader = new BufferedReader(new FileReader(outputFile));
 
 			// read text to temp string
 			String line = "";
 			String oldContent = "";
 			String table = "";
 			String makeTitleName = "";
 			String newContent = "";
 			while((line = reader.readLine()) != null) {
 				oldContent += line + "\n";
 			}
 			reader.close();
 
 			// write maketitle
 			makeTitleName = "\\title{" + machine.getName() + "}\n";
 			makeTitleName += "\\author{Author: " + machine.getAuthor() + "}";
 
 			// write automata nodes
 			table += writeTableForStates(machine);
 			
 			String description = "";
 			if (machine.getDescription() != "") {
 				description = machine.getDescription();
				Pattern p = Pattern.compile("\n");
				Matcher m = p.matcher(description);
				description = m.replaceAll("\\\\\\\\\n");
 			}
 			newContent = oldContent.replace("{PLACEHOLDER_TEXT}", description);
 			newContent = newContent.replace("{PLACEHOLDER_MAKETITLE}", makeTitleName);
 			newContent = newContent.replace("{PLACEHOLDER_TABLE}", table);
 
 			FileWriter writer = new FileWriter(outputFile);
 			writer.write(newContent);
 			writer.close();
 		}
 		catch (IOException e) {
 		}
 	}
 	
 	private static String writeTableForStates(TuringMachine machine) {
 		String table = "";
 		for (int i = 0; i < machine.getStates().size(); i++) {
 			boolean stateGotEdge = false;
 			State state = machine.getStates().get(i);
 			table += "\\subsubsection*{$" + state.getName() + "$";
 			if (state.isStartState()) {
 				table += " (start state)";
 			}
 			else if (state.isFinalState()) {
 				table += " (final state)";
 			}
 			table += ":}\n\n";
 			table += "\\begin{longtable}{c|c|c|c}\n";
 			table += "read & write & action & next state\\\\\n";
 			table += "\\hline\n";
 			for (int j = 0; j < machine.getEdges().size(); j++) {
 				if (machine.getEdges().get(j).getFrom().equals(state)) {
 					stateGotEdge = true;
 					Edge edge = machine.getEdges().get(j);
 					for (int k = 0; k < edge.getTransitions().size(); k++) {
 						table += writeTransitionToLatex(edge.getTransitions().get(k));
 						table += " & $" + edge.getTo() + "$\\\\\n";
 					}
 				}
 			}
 			if (!stateGotEdge) {
 				table += "-- & -- & --& --\n";
 			}
 			table += "\\end{longtable}\n\n";
 		}
 		return table;
 	}
 	
 	private static String writeTransitionToLatex(Transition transition) {
 		String entry = "";
 		ArrayList<Character> action = transition.getAction();
 		ArrayList<Character> read = transition.getRead();
 		ArrayList<Character> write = transition.getWrite();
 		String actionString = "<";
 		String readString = "<";
 		String writeString = "<";
 		for (int j = 0; j < action.size(); j++) {
 			actionString += "" + escapeHash(action.get(j));
 			readString += "" + escapeHash(read.get(j));
 			writeString += "" + escapeHash(write.get(j));
 			if (j < action.size()-1) {
 				actionString += ",";
 				readString += ",";
 				writeString += ",";
 			}
 			else if (j == action.size()-1) {
 				actionString += ">";
 				readString += ">";
 				writeString += ">";
 			}
 		}
 		entry += "$" + readString + "$ & $" + writeString + "$ & $" + actionString + "$";
 		return entry;
 	}
 
 	/**
 	 * Checks for hashes and escapes them
 	 * @param charToCheck Character to check for a hash
 	 * @return The initial character or an escaped hash
 	 */
 	private static String escapeHash(char charToCheck) {
 		if (charToCheck == '#') {
 			return "\\#";
 		}
 		return "" + charToCheck;
 	}
 
 	/**
 	 * Returns the value of an element with a certain tag
 	 * @param tag Tag you want to read
 	 * @param currentElement The current element you want to read from
 	 * @return The value of that tag
 	 * @throws IOException Thrown if not tag with the specified name is found or if it is not unique (used multiple times)
 	 */
 	public static String getTagValue(String tag, Element currentElement) throws IOException {
 		NodeList nodeList = currentElement.getElementsByTagName(tag);
 		if (nodeList.getLength() > 1) {
 			throw new IOException("The tag '" + tag + "' is ambigious because it was used multiple times on '" 
 					+ currentElement.getNodeName() + "'.");
 		}
 		if (nodeList.getLength() < 1) {
 			throw new IOException("Missing tag '" + tag + "' on '" + currentElement.getNodeName() + "'.");
 		}
 		NodeList childList = nodeList.item(0).getChildNodes();
 
 		Node child = null;
 		for (int i = 0; i < childList.getLength(); i++) {
 			child = childList.item(i);
 			if (child.getNodeType() != Node.TEXT_NODE) {
 				break; //ignore attributes etc.
 			}
 		}
 
 		if (child != null) {
 			return child.getNodeValue();
 		}
 		else {
 			System.out.println("WARNING: Tag '" + tag + "' on '" + currentElement.getNodeName() + "' is empty.");
 			return "";
 		}
 	}
 
 	/**
 	 * Gets a child element node with a specific name that is a child of the currentElement
 	 * @param tag The name of the child element to search
 	 * @param currentElement The parent element of the child to search
 	 * @return The child node with the specified name
 	 * @throws IOException Thrown if the child not is not unique (if the tag is used multiple times) or if the specified tag is not existent
 	 */
 	public static Element getChildElement(String tag, Element currentElement) throws IOException {
 		NodeList nodeList = currentElement.getElementsByTagName(tag);
 		Node node = null;
 		for (int i=0; i < nodeList.getLength(); i++) {
 			if (node != null && nodeList.item(i).getNodeType() == Node.ELEMENT_NODE ) {
 				throw new IOException("Multiple tag '" + tag + "' on '" + currentElement.getNodeName() + "' is not allowed.");
 			}
 			if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
 				node = nodeList.item(i);
 			}
 		}
 		if (node == null) {
 			throw new IOException("Expected missing tag '" + tag + "' on '" + currentElement.getNodeName() + "'.");
 		}
 		Element element = (Element) node;
 		return element;
 	}
 	
 	/**
 	 * Gets an attribute integer value of an attribute with a specific name that is child of the currentElement
 	 * @param attribute_name The name of the attribute to search
 	 * @param currentElement The parent element of the child to search
 	 * @return The attribute integer value, or 0 if the attribuet is not existent
 	 * @throws IOException (Deprecated) Thrown if the child if the specified attribute is not existent or is not an integer value
 	 */
 	public static int getAttributeValueInt(String attribute_name, Element currentElement) throws IOException {
 		String attrString = currentElement.getAttribute(attribute_name);
 		try {
 			return Integer.parseInt(attrString);
 		}
 		catch (NumberFormatException e){
 			System.out.println("WARNING: Expected integer value in attribute '" + attribute_name + "' for element '" + currentElement.getNodeName() + "' but found '" + attrString + "'. Treating as zero.");
 
 			return 0;
 		}
 	}
 
 }
