 package edu.stockton;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.ListIterator;
 
 import javax.swing.text.html.HTMLDocument.Iterator;
 
 /**
  * Top level processor responsible for overall ATC
  * command parsing. Understands passed ATC command
  * strings, utilizes Callsign, Number, and Instruction Engines
  * to return a fully parsed ATC command.
  *
  */
 public class LanguageProcessor {
 	private static boolean verbose = false;
 	private static boolean debug = false;
 	
 	/**
 	 * Interprets a complete ATC command string.
 	 * Parses the string and returns an ATCCommand object.
 	 * @param command The ATC command sentence
 	 * @return An ATCCommand object containing the parsed elements
 	 */
 	public static ATCCommand parse(String command) throws ParseException {
 		
 		ArrayList<String> tokens = new ArrayList<String>();
 		ArrayList<Component> components = new ArrayList<Component>();
 		
 		String[] words = command.split("[ -]");
 		for(String word : words) {
 			tokens.add(word);
 		}
 		
 		// Tag each token and group
 		String currentTag = "";
 		String componentText = "";
 				
 		for(int i = 0; i < tokens.size(); i++) {
 			
 			// Disregard any common filler words
 			if(tokens.get(i).equalsIgnoreCase("um") || tokens.get(i).equalsIgnoreCase("uh") ) continue;
 			
 			String thisTag = tag(tokens.get(i));
 			
 			if(debug) System.out.println("[LP] tagging token '" + tokens.get(i) + "' => " + thisTag);
 			
 			if(thisTag.equals(currentTag) && i < tokens.size()-1) {
 				componentText += tokens.get(i) + " ";	
 			} else if(i == 0) {
 				currentTag = thisTag;
 				componentText += tokens.get(i) + " ";
 			} else {
 				if(i == tokens.size() - 1 && thisTag.equals(currentTag)) { 
 					componentText += tokens.get(i) + " ";
 					Component thisComponent = new Component(componentText, currentTag);
 					components.add(thisComponent);
 					componentText = "";
 					componentText += tokens.get(i) + " ";
 					currentTag = thisTag;
 				} else if(i == tokens.size() - 1) {
 					Component thisComponent = new Component(componentText, currentTag);
 					components.add(thisComponent);
 					Component lastComponent = new Component(tokens.get(i), thisTag);
 					components.add(lastComponent);
 				} else {
 					Component thisComponent = new Component(componentText, currentTag);
 					components.add(thisComponent);
 					componentText = "";
 					componentText += tokens.get(i) + " ";
 					currentTag = thisTag;
 				}
 				
 			}
 
 		}
 		
 		if(verbose) {
 			System.out.println("Command components:");
 			for(Component c : components) {
 				System.out.println("[" + c.getType() + "] " + c.getText());
 			}
 		}
 		
 		String recipient = "";
 		
 		// Do conversions for each token
 		for(int i = 0; i < components.size(); i++) {
 			Component current = components.get(i);
 			String type = current.getType();
 			
 			if(type.equals("number")) {	
 				if(debug) System.out.print("[LP] ");
 				if(verbose || debug) System.out.print("'" + current.getText() + "' => ");
 				
 				current.setText(NumberEngine.toNumeric(current.getText()));
 				components.set(i, current);
 				
 				if(verbose || debug) System.out.println("'" + current.getText() + "'");
 				
 				// Recognize recipient and build recipient string
 				if(components.get(i-1).getType() == "callsign") {
 					recipient = components.get(i-1).getText() + components.get(i).getText();
 					
 					if(debug) System.out.print("[LP] ");
 					if(verbose || debug) System.out.println("Recipient identified: " + recipient);
 				}
 				
			} else if(type.equals("callsign") || type.equals("unidentified") && CallsignEngine.isCallsign(current.getText())) {
 				if(debug) System.out.print("[LP] ");
 				if(verbose || debug) System.out.print("'" + current.getText() + "' => ");
 				
 				if(type.equals("unidentified")) current.setType("callsign");
 				
 				current.setText(CallsignEngine.telephonyToDesignator(current.getText()));
 				components.set(i, current);
 				
 				if(verbose || debug) System.out.println("'" + current.getText() + "'");
 			}
 		}
 		
		if(recipient == "") throw new ParseException("No recognized recipient");
 		
 		String phrase = "";
 		for(Component component : components) {
 			phrase += component.getText() + " ";
 		}
 		
 		if(verbose) System.out.println("Looking for valid instructions...");
 		
 
 		ParsedInstruction instruction = InstructionEngine.parse(phrase);
 		
 		if(instruction == null) System.out.println("No instruction");
 		
 		if(verbose) {
 			System.out.println("Instruction found");
 			System.out.println("phrase: " + instruction.getPhrase());
 			System.out.println("instr match: " + instruction.getInstRegex());
 			System.out.println("type: " + instruction.getType());
 			System.out.println("param match: " + instruction.getParamRegex());
 			System.out.println("param: " + instruction.getParam());
 		}
 		
 		ATCCommand parsed = new ATCCommand(recipient, instruction.getType(), instruction.getParam());
 		
 		if(verbose) System.out.println("Complete");
 		
 		return parsed;
 
 	}
 	
 	/**
 	 * Tags a passed token string as a number, callsign, or otherwise
 	 * unidentified.
 	 * @param token The string to tag
 	 * @return The tag (either "number", "callsign", or "unidentified")
 	 */
 	private static String tag(String token) {
 		String tag = "unidentified";
 		if(NumberEngine.isNumber(token)) tag = "number";
 		if(CallsignEngine.isCallsign(token)) tag = "callsign";
 		return tag;
 	}
 	
 	/**
 	 * Sets verbose option, which prints parsing details.
 	 * @param s true or false to set on/off
 	 */
 	public static void setVerbose(boolean s) {
 		verbose = s;
 	}
 	
 	/**
 	 * Sets debug option, which prints low-level process details
 	 * @param s true or false to set on/off
 	 */
 	public static void setDebug(boolean s) {
 		debug = s;
 	}
 	
 	
 }
