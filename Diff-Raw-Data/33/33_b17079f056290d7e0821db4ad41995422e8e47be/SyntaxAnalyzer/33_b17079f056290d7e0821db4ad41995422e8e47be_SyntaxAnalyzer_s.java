 package os;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 
 /**
  * shell/SysntaxAnalyzer.java 
  * <br><br>
  * 
  * Parse command to processes, where each will have his pipes
  * also finds out if command should be executed in background.
  * 
  * SHEL GRAMMAR:
  * 
  * COMMAND -> PROCESS ARGUMENT PIPE ASYNC
  * PROCESS -> string
  * ARGUMENT -> string || >string || <string || ARGUMENT ARGUMENT || "ARGUMENT" || e
  * PIPE -> | PROCESS ARGUMENT PIPE || e
  * ASYNC -> & || e
  * 
  * 
  * @author Lukáš Hain
  * @version 0.1
  * 
  * @team <i>OutOfMemory</i> for KIV/OS 2013
  * @teamLeader Radek Petruška radekp25@students.zcu.cz
  * 
  */
 public class SyntaxAnalyzer {
 
 	/**
 	 * List of parsed processes.
 	 */
 	private LinkedList<ProcessAnalysis> processList = new LinkedList<SyntaxAnalyzer.ProcessAnalysis>();
 
 
 	//============= TEMP VARIABLES ================================
	private String actualProcessT;
 	private LinkedList<String> argumentsT = new LinkedList<String>();
	private String stdInT;
	private String stdOutT;
 	//=============================================================
 
 	private static String errorMessage = "";
 
 	/**
 	 * This method controls command processing. 
 	 * @param command string containing command
 	 * @param currentDirectory which will be used to execute processes
 	 * @return CommandAnalysis object which include array of processes and boolean if it should be run in background
 	 */
 	public CommandAnalysis processCommand(String command, String currentDirectory){
 		boolean asyncFound = false;
 
 		if(command.length() > 0){
 
 			String[] parts = parseCommandToParts(command);
 			if(parts == null){
 				return new CommandAnalysis(errorMessage);
 			}
			
 			for(int i = 0; i < parts.length; i++){
 				System.out.println(i + ". " + parts[i]);
 			}
 
 			for(int i = 0; i < parts.length; i++){
 				if(isPipe(parts[i])){
 					System.out.println("output of last process will be redirected to input of next");
 					continue;
 				} else if(isAsync(parts[i])){
 					asyncFound = true;
 					System.out.println("End of parsing. Command will be executed in background.");
 					break;
 				}
 
 				resetValues();
 				this.actualProcessT = parts[i];
 
 				if(this.actualProcessT == null || this.actualProcessT.length() < 1){
 					return new CommandAnalysis(errorMessage);
 				}
 
 				i = parseProcess(parts, ++i, currentDirectory);
 				if(i == -1){
 					return new CommandAnalysis(errorMessage);
 				}
 				System.out.println("i=" +i);
 				System.out.println("Process: " + this.actualProcessT + ", arguments: "+ this.argumentsT.toString() +", stdIn: " + this.stdInT + ", stdOut: " + this.stdOutT);
 				this.processList.add(new ProcessAnalysis(this.actualProcessT, toStringArray(this.argumentsT), this.stdInT, this.stdOutT)); 
 
 			}
 
 			resetValues();
 
 		}
 
 		return new CommandAnalysis(toProcessArray(this.processList), asyncFound);
 	}
 
 	/**
 	 * Transforms list to array.
 	 * 
 	 * @param list list to be transformed
 	 * @return array of processes
 	 */
 	private static ProcessAnalysis[] toProcessArray(LinkedList<ProcessAnalysis> list) {
 		ProcessAnalysis[] processAnalysisArray = new ProcessAnalysis[list.size()];
 		for(int i = 0; i < processAnalysisArray.length; i++){
 			processAnalysisArray[i] = list.get(i);
 		}
 		return processAnalysisArray;
 	}
 
 	/**
 	 * Resets temp values.
 	 */
 	private void resetValues() {
 		this.actualProcessT = "";
 		this.argumentsT.removeAll(this.argumentsT);
 		this.stdInT = "";
 		this.stdOutT = "";
 	}
 
 	/**
 	 * First step of command processing. It parses command to parts without any knowledge
 	 * of where they belong or anything else. Its just divided to separate parts.
 	 * 
 	 * @param command command to be parsed
 	 * @return array of parts
 	 */
 	synchronized private static String[] parseCommandToParts(String command) {
 		LinkedList<String> listOfParts = new LinkedList<String>();
 		char[] commandCharArray = command.toCharArray();
 		char[] specialChars = {'|','>','<','&'};
 
 
 		int offset = 0;
 		int lenght = 0;
 
 		char temp;
 		boolean inQuote = false;
 		boolean wasSpecial = false;
 
 		for(int i = 0; i < commandCharArray.length; i++){
 			temp = commandCharArray[i];
 
 			if(!inQuote){
 				if(Character.isWhitespace(temp)){
 					if(lenght > 0){
 						addPart(listOfParts, commandCharArray, offset, lenght);
 					}
 					lenght = 0;
 					offset = i+1;
 				} else if(isOneOf(specialChars, temp)){
 					if(wasSpecial){ //dva ridici znaky za sebou
 						errorMessage = "Error at " + temp + ".\nThere cant be two special chars after each other.";
 						return null;
 					}
 					if(lenght > 0){
 						addPart(listOfParts, commandCharArray, offset, lenght);
 						offset = i;
 						lenght = 0;
 					}
 
 					addPart(listOfParts, commandCharArray, offset, lenght + 1);
 					wasSpecial = true;
 
 					offset = i + 1;
 					lenght = 0;
 				} else if(isQuote(temp)){
 					wasSpecial = false;
 					inQuote = true;
 					offset = i + 1;
 					lenght = 0;
 				} else{
 					wasSpecial = false;
 					lenght++;
 				}
 			} else{
 				if(isQuote(temp)){
 					inQuote = false;
 					addPart(listOfParts, commandCharArray, offset, lenght);
 					lenght = 0;
 					offset = i+1;
 				} else{
 					lenght++;
 				}
 			}
 		}
 
 		if(inQuote){ //je konec prikazu a chybi mi ukoncujici uvozovka
 			errorMessage = "Missing closing quote.";
 			return null;
 		}
		
 		if(lenght > 0){
 			addPart(listOfParts, commandCharArray, offset, lenght);
 		}
 
 		return toStringArray(listOfParts);
 	}
 
 	/**
 	 * Parses individual processes it assign std. in, out and err for each one
 	 * if its specified by user.
 	 * 
 	 * @param parts array of parts
 	 * @param i where in array command starts
 	 * @param currentDirectory directory where process will be later executed
 	 * @return position in command parts array
 	 */
 	private int parseProcess(String[] parts, int i, String currentDirectory) {
 		int position = i;
 
 		this.argumentsT.add(currentDirectory);
 
 		while(position < parts.length && !isPipe(parts[position]) && !isAsync(parts[position])){
 
 			try{
 				if(isOutput(parts[position])){
					this.stdOutT = parts[position+1];
					position++;
 				} else if(isInput(parts[position])){
					this.stdInT = parts[position+1];
					position++;
 				} else{
 					this.argumentsT.add(parts[position]);
 				}
 
 				position++;
 			} catch(ArrayIndexOutOfBoundsException ex){
 				errorMessage = "Error after " + parts[position] + " expected string.";
 				return -1;
 			}
 		}
 
 		return position - 1;
 	}
 
 
 
 	private static boolean isInput(String string) {
 		if(string.compareTo("<") == 0){
 			return true;
 		}
 		return false;
 	}
 
 	private static boolean isOutput(String string) {
 		if(string.compareTo(">") == 0){
 			return true;
 		}
 		return false;
 	}
 
 	private static boolean isAsync(String string) {
 		if(string.compareTo("&") == 0){
 			return true;
 		}
 		return false;
 	}
 
 	private static boolean isPipe(String string) {
 		if(string.compareTo("|") == 0){
 			return true;
 		}
 		return false;
 	}
 
 
 	private static boolean isQuote(char c) {
 		return c == '"';
 	}
 
 
 	private static String[] toStringArray(LinkedList<String> list) {
 		String[] stringArray = new String[list.size()];
 		for(int i = 0; i < stringArray.length; i++){
 			stringArray[i] = list.get(i);
 		}
 		return stringArray;
 	}
 
 	private static boolean isOneOf(char[] specialChars, char temp) {
 		for(int i = 0; i < specialChars.length; i++){
 			if(specialChars[i] == temp){
 				return true;
 			}
 		}
 		return false;
 	}
 
 
 	private static void addPart(LinkedList<String> listOfParts, char[] commandCharArray, int offset, int lenght) {
 		listOfParts.add(new String(commandCharArray, offset, lenght));
 	}
 
 	public class ProcessAnalysis{
 		String nameOfProcess;
 		String[] arguments;
 		String stdIn;
 		String stdOut;
 
 
 		public ProcessAnalysis(String nameOfProcess, String[] arguments, String stdIn, String stdOut/*, String stdErr*/){
 			setNameOfProcess(nameOfProcess);
 			setArguments(arguments);
 			setStdIn(stdIn);
 			setStdOut(stdOut);
 		}
 
 
 		public String getNameOfProcess() {
 			return this.nameOfProcess;
 		}
 
 
 		public String[] getArguments() {
 			return this.arguments;
 		}
 
 
 		public String getStdIn() {
 			return this.stdIn;
 		}
 
 
 		public String getStdOut() {
 			return this.stdOut;
 		}
 
 
 
 		public void setNameOfProcess(String nameOfProcess) {
 			try{
 				this.nameOfProcess = Character.toUpperCase(nameOfProcess.charAt(0)) + nameOfProcess.substring(1).toLowerCase();
 			} catch(IndexOutOfBoundsException ex){
 				//TODO
 			}
 		}
 
 
 		public void setArguments(String[] arguments) {
 			this.arguments = arguments;
 		}
 
 
 		public void setStdIn(String stdIn) {
 			this.stdIn = stdIn;
 		}
 
 
 		public void setStdOut(String stdOut) {
 			this.stdOut = stdOut;
 		}
 
 
 
 		@Override
 		public String toString() {
 			return "Name: " + this.nameOfProcess
 					+ ", arguments: " + Arrays.toString(this.arguments) + ", stdIn: "
 					+ this.stdIn + ", stdOut: " + this.stdOut;
 		}
 
 
 
 	}
 }
 
 
 
