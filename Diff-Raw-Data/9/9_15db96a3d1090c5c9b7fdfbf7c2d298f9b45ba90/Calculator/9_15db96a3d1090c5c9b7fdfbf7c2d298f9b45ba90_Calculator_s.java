 import java.util.Stack;
 
 public class Calculator {
 
 	public static void main(String[] args) {
 
 		// make sure we have an expression on the command line
 		if (args.length != 1) {
 			System.err.println("Usage: Calculator <expression>");
 			return;
 		}
 		String expression = args[0];
 
 		// split expression up into tokens
 		String[] tokens = expression.split(" ");
 
 		// for each token in the expression ...
 		Stack<Integer> stack = new Stack<Integer>();
 		for (String token : tokens) {
 			if (!handleOperator(stack, token) &&
 				!handleNumber(stack, token)) {
 				throw new IllegalArgumentException("garbage");
 			}
 		}
 
 		// the result is the last thing left on the stack
 		int result = stack.pop();
 		System.out.println("result: " + result);
 	}
 
 	public static boolean handleNumber(Stack<Integer> stack, String token) {
 		try {
 			// if the token is an integer, push it
 			int number = Integer.parseInt(token);
 			stack.push(number);
 			return true;
 		} catch (NumberFormatException e) {
 			return false;
 		}
 	}
 
 	public static boolean handleOperator(Stack<Integer> stack, String token) {
 		// if the token is an operator, pop two numbers,
 		// perform the op and push the result
		int rhs = stack.pop(), lhs = stack.pop();
 		if (token.equals("+")) {
 			stack.push(lhs + rhs);
 		} else if (token.equals("-")) {
 			stack.push(lhs - rhs);
 		} else if (token.equals("*")) {
 			stack.push(lhs * rhs);
 		} else if (token.equals("/")) {
 			stack.push(lhs / rhs);
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 }
