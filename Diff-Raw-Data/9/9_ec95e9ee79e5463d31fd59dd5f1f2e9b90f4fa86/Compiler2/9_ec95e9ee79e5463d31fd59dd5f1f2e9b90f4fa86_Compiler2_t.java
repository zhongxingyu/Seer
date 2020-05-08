 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 
 public class Compiler {
 	Scanner scanner;
 	int[] memory;
 	ArrayList<Integer> constants;
 	HashMap<Integer, Integer> line_numbers;
 	HashMap<String, Integer> variables;
 	int line_number, pointer, data_pointer;
 
 	public Compiler(File file) {
 		scanner = new Scanner(file);
 		memory = new int[100];
 		constants = new ArrayList<Integer>();
 		line_numbers = new HashMap<Integer, Integer>();
 		variables = new HashMap<String, Integer>();
 		line_number = 0;
 		pointer = 0;
 		data_pointer = 99;
 	}
 
 	public int[] compile() {
 		while(scanner.hasNextLine()) {
 			if(pointer >= data_pointer)
 				throw new OutOfMemoryException();
 
 			String[] command = scanner.nextLine().split(" ");
 
 			int next_line_number = Integer.parseInt(command[0]);
 			if(next_line_number <= line_number)
 				throw new LineNumberException();
 			line_numbers.put(next_line_number, pointer);
 			line_number = next_line_number;
 
 			if(command[1].equalsIgnoreCase("rem")) //Ingore comment lines
 				continue;
 			else if(command[1].equalsIgnoreCase("input")) { //Make a new variable and remember it
 				if(!Character.isLetter(command[2].charAt(0)))
 					throw new InvalidVariableException();
 
 				variables.put(command[2], data_pointer);
 				memory[pointer] = 1000 + data_pointer;
 				data_pointer--;
 			}
 			else if(command[1].equalsIgnoreCase("print")) { //Simply print variable
 				if(!variables.containsKey(command[2]))
 					throw new UndefinedVariableException();
 
 				memory[pointer] = 1100 + variables.get(command[2]);
 			}
 			else if(command[1].equalsIgnoreCase("let")) { //If a variable doesn't exist, create it then parse the expression
 				if(!variables.containsKey(command[2])) {
 					variables.put(command[2], data_pointer);
 					data_pointer--;
 				}
 
				parseExpression(command[3], variables.get(command[2]));
 			}
 			else if(command[1].equalsIgnoreCase("goto")) { //Put a new goto
 				memory[pointer] = 4000 + Integer.parseInt(command[2]);
 			}
 			else if(command[1].equalsIgnoreCase("if")) { //Yay for if's
 				if(!command[3].equalsIgnoreCase("goto"))
 					throw new IllegalArgumentException();
 				parseRelation(command[2]);//Call parse relation
 				memory[pointer] = 4100 + Integer.parseInt(command[4]);//Branch 0 to "goto"
 			}
 			else if(command[1].equalsIgnoreCase("end")) //Put a halt
 				memory[pointer] = 4300;
 
 			pointer++;
 		}
 	}
 
 	private void parseRelation(String relation) {
 		Pattern.compile("([a-zA-Z0-9+-*/()]+)(>|<|>=|<=|==|!=)+([a-zA-Z0-9+-*/()]+)");
 	}
 
 	private void parseExpression(String expression, int value_pointer) {
 	}
 }
