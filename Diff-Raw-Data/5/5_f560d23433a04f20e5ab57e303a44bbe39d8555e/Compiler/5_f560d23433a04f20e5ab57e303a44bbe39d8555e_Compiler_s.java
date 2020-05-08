 /**
  * Compiles the SIMPLE program into a memory array that can be run in an SML interpreter.
  * 
  * @author Foster Mclane and Jonathan Lowe
  */
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.Scanner;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Compiler {
 	private final static Pattern relation_pattern = Pattern.compile("(.+)(>|<|>=|<=|==|!=)+(.+)");
 	private final static String operators = "+-*/()";
 	private final static Pattern expression_pattern = Pattern.compile("([a-zA-Z0-9+\\-*/]+)");
 
 	Scanner scanner;
 	int[] memory;
 	ArrayList<Integer> constants;
 	ArrayList<Integer> line_number_list;
 	HashMap<Integer, Integer> line_numbers;
 	HashMap<String, Integer> variables;
 	int last_line_number, pointer, data_pointer;
 
 	/**
 	 * Constructs a new Compiler object given a file
 	 * 
 	 * @param file The SIMPLE file
 	 * @throws FileNotFoundException Thrown if the file does not exist
 	 */
 	public Compiler(File file) throws FileNotFoundException {
 		scanner = new Scanner(file);
 		memory = new int[100];
 		constants = new ArrayList<Integer>();
 		line_number_list = new ArrayList<Integer>();
 		line_numbers = new HashMap<Integer, Integer>();
 		variables = new HashMap<String, Integer>();
 		last_line_number = -1;
 		pointer = 0;
 		data_pointer = 99;
 	}
 
 	/**
 	 * Return the current SIMPLE line number
 	 * 
 	 * @return The line number
 	 */
 	public int getLineNumber() {
 		return last_line_number;
 	}
 
 	/**
 	 * Return the current SML pointer
 	 * 
 	 * @return The memory pointer
 	 */
 	public int getPointer() {
 		return pointer;
 	}
 
 	/**
 	 * Compile the SIMPLE into an SML memory array
 	 * 
 	 * @return The SML memory array
 	 * 
 	 * @throws OutOfMemoryException Ran out of SML memory
 	 * @throws ArgumentException If without a goto
 	 * @throws InvalidVariableException Variable doesn't start with a letter
 	 * @throws NumberFormatException Invalid number
 	 * @throws SyntaxException SIMPLE syntax error
 	 * @throws GotoException Goto nonexistent line
 	 * @throws LineNumberException Line numbers don't increase
 	 * @throws UndefinedVariableException Variable accessed before it exists
 	 */
 	public int[] compile() throws OutOfMemoryException, ArgumentException, InvalidVariableException, NumberFormatException, SyntaxException, GotoException, LineNumberException, UndefinedVariableException {
 		while(scanner.hasNextLine()) {
 			if(pointer >= data_pointer)
 				throw new OutOfMemoryException();
 
 			String[] command = scanner.nextLine().split(" "); //Everything is separated by spaces
 
 			//Ignore comment lines
 			if(command[0].equalsIgnoreCase("rem") || command[1].equalsIgnoreCase("rem"))
 				continue;
 
 			//Double check line numbers
 			int line_number = Integer.parseInt(command[0]);
 			if(line_number <= last_line_number)
 				throw new LineNumberException();
 
 			//Add it to the hashmap and line number array
 			if(!line_number_list.contains(line_number))
 				line_number_list.add(line_number);
 			line_numbers.put(line_number, pointer);
 			last_line_number = line_number;
 
 			//Make a new variable and remember it
 			if(command[1].equalsIgnoreCase("input")) {
 				if(!Character.isLetter(command[2].charAt(0)))
 					throw new InvalidVariableException();
 
 				variables.put(command[2], data_pointer);
 				memory[pointer] = 1000 + data_pointer;
 				pointer++;
 				data_pointer--;
 			}
 			//Simply print variable
 			else if(command[1].equalsIgnoreCase("print")) {
 				if(!variables.containsKey(command[2]))
 					throw new UndefinedVariableException();
 
 				memory[pointer] = 1100 + variables.get(command[2]);
 				pointer++;
 			}
 			//If a variable doesn't exist, create it then parse the expression
 			else if(command[1].equalsIgnoreCase("let")) {
 				String[] params = command[2].split("=", 2);
 
 				if(params.length < 2)
 					throw new SyntaxException();
 
 				if(!Character.isLetter(params[0].charAt(0)))
 					throw new InvalidVariableException();
 
 				if(!variables.containsKey(params[0])) {
 					variables.put(params[0], data_pointer);
 					data_pointer--;
 				}
 
 				parseExpression(params[1], variables.get(params[0]));
 			}
 			//Put a new goto
 			else if(command[1].equalsIgnoreCase("goto")) {
 				int goto_line = Integer.parseInt(command[2]);
 				if(!line_number_list.contains(goto_line))
 					line_number_list.add(goto_line);
 
 				memory[pointer] = 14000 + line_number_list.indexOf(goto_line);
 				pointer++;
 			}
 			//Yay for if's
 			else if(command[1].equalsIgnoreCase("if")) {
 				if(!command[3].equalsIgnoreCase("goto"))
 					throw new ArgumentException();
 
 				int goto_line = Integer.parseInt(command[4]);
 				if(!line_number_list.contains(goto_line))
 					line_number_list.add(goto_line);
 
 				parseRelation(command[2], line_number_list.indexOf(goto_line)); //Call parse relation
 			}
 			//Put a halt
 			else if(command[1].equalsIgnoreCase("end")) {
 				memory[pointer] = 4300;
 				pointer++;
 			}
 		}
 
 		//Make sure we still have room for constants
 		if(pointer + constants.size() > data_pointer)
 			throw new OutOfMemoryException();
 
		//Space the constants on the end of the program
 		for(int i = 0; i < constants.size(); i++)
 			memory[pointer + i] = constants.get(i);
 
 		for(int i = 0; i < 100; i++) {
 			int opcode = memory[i] / 100;
 			switch(opcode) {
 				//Constants
 				case 120:
 				case 121:
 				case 130:
 				case 131:
 				case 132:
 				case 133:
 					//Take the 1 flag from the front then increment the constant index to point to the constant at the end of the program
 					//Equivalent to (opcode - 100) * 100 + pointer + memory[i] % 100
 					memory[i] = memory[i] - 10000 + pointer;
 					break;
					//Line numbers
 				case 140:
 				case 141:
 				case 142:
 					//Check that the line number exists
 					Integer line_number_pointer = line_numbers.get(line_number_list.get(memory[i] % 100));
 					if(line_number_pointer == null)
 						throw new GotoException();
 
 					memory[i] = (opcode  - 100) * 100 + line_number_pointer;
 					break;
 			}
 		}
 
 		pointer += constants.size();
 
 		return memory;
 	}
 
 	private void parseRelation(String relation, int goto_symbol) throws OutOfMemoryException, SyntaxException, NumberFormatException, UndefinedVariableException {
 		//Check relations based on regexes
 		Matcher matcher = relation_pattern.matcher(relation);
 		if(!matcher.matches())
 			throw new SyntaxException();
 
 		parseExpression(matcher.group(1), data_pointer);
 		data_pointer--;
 		parseExpression(matcher.group(3), data_pointer);
 
 		memory[pointer] = 2000 + data_pointer;
 		pointer++;
 
 		if(matcher.group(2).charAt(0) == '>') {
 			memory[pointer] = 3100 + data_pointer + 1; //First number
 			pointer++;
 			memory[pointer] = 14100 + goto_symbol; //Branch negative to "goto"
 			if(matcher.group(2).length() > 1 && matcher.group(2).charAt(1) == '=') {
 				pointer++;
 				memory[pointer] = 4100 + goto_symbol; //Also branch zero if equal to
 			}
 		}
 		else if(matcher.group(2).charAt(0) == '<') {
 			memory[pointer - 1] = 2000 + data_pointer + 1;
 			memory[pointer] = 3100 + data_pointer;
 			pointer++;
 			memory[pointer] = 14100 + goto_symbol;
 			if(matcher.group(2).length() > 1 && matcher.group(2).charAt(1) == '=') {
 				pointer++;
 				memory[pointer] = 14100 + goto_symbol;
 			}
 		}
 		else if(matcher.group(2).equals("==")) {
 			memory[pointer] = 3100 + data_pointer + 1;
 			pointer++;
 			memory[pointer] = 14200 + goto_symbol;
 		}
 		else if(matcher.group(2).equals("!=")) {
 			if(!constants.contains(-1))
 				constants.add(-1);
 
 			memory[pointer] = 3100 + data_pointer + 1;
 			pointer++;
 			memory[pointer] = 14100 + goto_symbol;
 			pointer++;
 			memory[pointer] = 13300 + constants.indexOf(-1);
 			pointer++;
 			memory[pointer] = 14100 + goto_symbol;
 		}
 
 		pointer++;
 		data_pointer--;
 	}
 
 	private void parseExpression(String expression, int value_pointer) throws OutOfMemoryException, SyntaxException, NumberFormatException, UndefinedVariableException {
 		//Check expressions based on regexes
 		Matcher matcher = expression_pattern.matcher(expression);
 		if(!matcher.matches())
 			throw new SyntaxException();
 
 		LinkedList<String> postfix_list = convertToPostfix(expression);
 		ListIterator<String> postfix = postfix_list.listIterator();
 
 		if(postfix_list.size() == 1) {
 			String value = postfix.next();
 			int value_symbol;
 			if(Character.isLetter(value.charAt(0))) {
 				//Check if variable exists before using it
 				if(!variables.containsKey(value))
 					throw new UndefinedVariableException();
 
 				value_symbol = variables.get(value);
 			}
 			else {
 				int number = Integer.parseInt(value);
 
 				if(!constants.contains(number))
 					constants.add(number);
 
 				//This is a constant so mark it
 				value_symbol = 10000 + constants.indexOf(number);
 			}
 
 			memory[pointer] = 2000 + value_symbol;
 			pointer++;
 			memory[pointer] = 2100 + value_pointer;
 			pointer++;
 
 			return;
 		}
 
 		int temp_data_pointer = data_pointer;
 		while(postfix.hasNext()) {
 			int operator = operators.indexOf(postfix.next());
 			if(operator != -1) {
 				if(temp_data_pointer < pointer)
 					throw new OutOfMemoryException();
 
 				postfix.previous();
 
 				String operand = postfix.previous();
 				int operand_symbol;
 				if(operand.charAt(0) == '.') {
 					operand_symbol = Integer.parseInt(operand.substring(1));
 				}
 				else if(Character.isLetter(operand.charAt(0))) {
 					if(!variables.containsKey(operand))
 						throw new UndefinedVariableException();
 
 					operand_symbol = variables.get(operand);
 				}
 				else {
 					int number = Integer.parseInt(operand);
 
 					if(!constants.contains(number))
 						constants.add(number);
 
 					//This is a constant so mark it
 					operand_symbol = 10000 + constants.indexOf(number);
 				}
 
 				String load = postfix.previous();
 				int load_symbol;
 				if(load.charAt(0) == '.') {
 					load_symbol = Integer.parseInt(load.substring(1));
 				}
 				else if(Character.isLetter(load.charAt(0))) {
 					if(!variables.containsKey(load))
 						throw new UndefinedVariableException();
 
 					load_symbol = variables.get(load);
 				}
 				else {
 					int number = Integer.parseInt(load);
 
 					if(!constants.contains(number))
 						constants.add(number);
 
 					//This is a constant so mark it
 					load_symbol = 10000 + constants.indexOf(number);
 				}
 
 				memory[pointer] = 2000 + load_symbol;
 				pointer++;
 				switch(operator) {
 					case 0:
 						memory[pointer] = 3000 + operand_symbol;
 						break;
 					case 1:
 						memory[pointer] = 3100 + operand_symbol;
 						break;
 					case 2:
 						memory[pointer] = 3300 + operand_symbol;
 						break;
 					case 3:
 						memory[pointer] = 3200 + operand_symbol;
 						break;
 				}
 				pointer++;
 				postfix.remove();
 				postfix.next();
 				postfix.remove();
 				postfix.next();
 				postfix.remove();
 				if(postfix.hasNext()) {
 					memory[pointer] = 2100 + temp_data_pointer;
 					postfix.add("." + temp_data_pointer);
 					temp_data_pointer--;
 				}
 				else {
 					memory[pointer] = 2100 + value_pointer;
 				}
 				pointer++;
 			}
 		}
 	}
 
 	private LinkedList<String> convertToPostfix(String infix) {
 		LinkedList<String> postfix = new LinkedList<String>();
 		Stack<Character> operator_stack = new Stack<Character>();
 		char[] chars = infix.toCharArray();
 		for(int i = 0; i < chars.length; i++) {
 			String term = new String();
 
 			//Get full term
 			while(operators.indexOf(chars[i]) == -1 || i == 0 || (operators.indexOf(chars[i - 1]) != -1 && operators.indexOf(chars[i]) == 1)) {
 				term += chars[i];
 				i++;
 
 				if(i >= chars.length) {
 					postfix.add(term);
 					while(!operator_stack.empty())
 						postfix.add(Character.toString(operator_stack.pop()));
 					return postfix;
 				}
 			}
 
 			postfix.add(term);
 			if(operator_stack.empty()) {
 				operator_stack.push(chars[i]);
 			}
 			else {
 				while(operators.indexOf(chars[i]) <= operators.indexOf(operator_stack.peek())) {
 					postfix.add(Character.toString(operator_stack.pop()));
 				}
 
 				operator_stack.push(chars[i]);
 			}
 		}
 
 		return postfix;
 	}
 }
