 import java.util.Stack;
 
 
 public class MiniDc {
 	private Stack<Double> runningStack;
 	private Stack<String> errorStack;
 	private Stack<String> printStack;
 
 	MiniDc(){
 		runningStack = new Stack<Double>();
 		errorStack = new Stack<String>();
 		printStack = new Stack<String>();
 	}
 
 	public boolean parseInput(String input){
 		boolean setToNeg = false;
 		input = input.replaceAll("\\s",""); //remove whitespace and non characters
 		if ((input.charAt(0) == '/') && input.length() == 1){
 			boolean return_ = divTopTwo();
 			return return_;
 		}
 		else if ((input.charAt(0) == '*') && input.length() == 1){
 			boolean return_ = mulTopTwo();
 			return return_;
 		}
 		else if(input.contains("*") && input.length() > 1){
 			errorStack.push("If you want to multiply 2 numbers, " +
 					"push them to the stack, then send * alone");
 			return false;
 		}
 		else if ((input.charAt(0) == '-') && input.length() == 1){
 			boolean return_ = subTopTwo();
 			return return_;
 		}
 		else if(input.contains("-") && (input.charAt(0) != '-') && input.length() > 1){
 			errorStack.push("If you want to subtract 2 numbers, " +
 				"push them to the stack in the order you want to subtrac. Then send - alone");
 			return false;
 		}
 		else if ((input.charAt(0) == '+') && input.length() == 1){
 				boolean return_ = addTopTwo();
 				return return_;
 		}
 		else if(input.contains("+") && input.length() > 1){
 			errorStack.push("If you want to add 2 numbers, push them to the stack first then send + alone");
 			return false;
 		}
 		else if ((input.charAt(0) == 'f' || input.charAt(0) == 'F') && input.length() == 1){
 			if(!runningStack.isEmpty()){
 				dumpToPrint();
 				return true;
 			}
 			else{
 				errorStack.push("Stack Empty");
 				return false;
 			}
 		}
 		else if ((input.charAt(0) == 'n' || input.charAt(0) == 'N') && input.length() == 1){
 			if(!runningStack.isEmpty()){
 				printStack.push(numberToString(runningStack.pop()));
 				return true;
 			}
 			else{
 				errorStack.push("Stack Empty");
 				return false;
 			}
 		}
 		else if((input.charAt(0) == 'n' || input.charAt(0) == 'N') && input.length() > 1){
 			errorStack.push("To print please use n or N alone, if you want to Pop");
 			return false;
 		}
 		else if ((input.charAt(0) == 'p' || input.charAt(0) == 'P') && input.length() == 1){
 			if(!runningStack.isEmpty()){
 				printStack.push(numberToString(runningStack.peek()));
 				return true;
 			} else{
 				errorStack.push("Stack Empty");
 				return false;
 			}
 		}
 		else if((input.charAt(0) == 'p' || input.charAt(0) == 'P') && input.length() > 1){
 			errorStack.push("To print please use p or P alone");
 			return false;
 		}
 		else if (input.charAt(0) == '_' && input.length() > 1){
 			input = input.replaceAll("_","");
 			setToNeg = true;
 		}
 		else if(input.charAt(0) == '-' && input.length() > 1){ //used - instead of _ for negative number
 			errorStack.push("Improper negative symbol, try _ instead of -");
 			return false;
 		}
 		try{
 			double temp = Double.parseDouble(input);
 			if(temp == Double.POSITIVE_INFINITY || temp == Double.NEGATIVE_INFINITY || Double.isNaN(temp) ){
 				errorStack.push("Out of range.");
 				return false;
 			}
 			if(setToNeg == true) temp = temp*-1.0;
 			runningStack.push(temp);
 		} catch(NumberFormatException e){
 			errorStack.push("You entered invalid input, input junked.");
 			return false;
 		}
 		return true;
 	}
 
 	public String peekPrintStack(){
 		return printStack.peek();
 	}
 
 	public double peekFromStack(){
 		return runningStack.peek();
 	}
 
 	public boolean isPrintStackEmpty(){
 		return printStack.isEmpty();
 	}
 
 	public String popFromPrintStack(){
 		return printStack.pop();
 	}
 
 	public String peekError(){
 		return errorStack.peek();
 	}
 
 	private String numberToString(double number){
 		if((int)number == number){
 			return Integer.toString((int)number);
 		}
 		else return Double.toString(number);
 	}
 
 	private void dumpToPrint(){
 		for(int i =0;i<runningStack.size();i++)
 		{
 			String item = numberToString(runningStack.get(i));
 			printStack.push(item);
 		} 
 	}
 
 	private boolean addTopTwo(){
 		double a = 0;
 		double b = 0;
 		try{
 			b = runningStack.pop();
 		} catch(Exception e){
 			errorStack.push("Stack Empty");
 			return false;
 		}
 		try{
 			a = runningStack.pop();
 		} catch(Exception e){
 			errorStack.push("Only one number on stack, can't do operation");
 			return false;
 		}
 		double result = a + b;
 		runningStack.push(result);
 		return true;
 	}
 	
 	private boolean subTopTwo(){
 		double a = 0;
 		double b = 0;
 		try{
 			b = runningStack.pop();
 		} catch(Exception e){
 			errorStack.push("Stack Empty");
 			return false;
 		}
 		try{
 			a = runningStack.pop();
 		} catch(Exception e){
 			errorStack.push("Only one number on stack, can't do operation");
 			return false;
 		}
 		double result = a - b;
 		runningStack.push(result);
 		return true;
 	}
 	
 	private boolean mulTopTwo(){
 		double a = 0;
 		double b = 0;
 		try{
 			b = runningStack.pop();
 		} catch(Exception e){
 			errorStack.push("Stack Empty");
 			return false;
 		}
 		try{
 			a = runningStack.pop();
 		} catch(Exception e){
 			errorStack.push("Only one number on stack, can't do operation");
 			return false;
 		}
 		double result = a * b;
 		runningStack.push(result);
 		return true;
 	}
 
 	private boolean divTopTwo(){
 		double a = 0;
 		double b = 0;
 		try{
 			b = runningStack.pop();
 		} catch(Exception e){
 			errorStack.push("Stack Empty");
 			return false;
 		}
 		try{
 			a = runningStack.pop();
 		} catch(Exception e){
 			errorStack.push("Only one number on stack, can't do operation");
 			return false;
 		}
 		double result = a / b;
 		runningStack.push(result);
 		return true;
 	}
 	
 	public boolean isStackEmpty(){
 		return runningStack.isEmpty();
 	}
 
 
 }
