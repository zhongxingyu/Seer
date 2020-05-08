 package main;
 
 import scanner.Scanner;
 
 public class Interpreter {
 	public static void main(String[] args) {
 		Scanner scanner = new Scanner("Hello [\"Hello, world!\" \"Hi again!\"] " +
 				"/* nested /*comment*/");
 		try {
 			scanner.scan();
			
 			System.out.println(scanner.code);
			
 			System.out.println("Tokens: ");
 			for(int i = 0; i < scanner.tokens.size(); i++) {
				System.out.println("Type: '" + scanner.tokens.get(i).getType().toString() + 
						"'. Value: '" + ((String)scanner.tokens.get(i).getValue()) + "'.");
 			}
		} catch (SyntaxException e) {
			System.out.println(e);
 		}
 	}
 }
