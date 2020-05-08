 package de.schiemenz.stackcalc;
 
 import java.text.*;
 import java.util.*;
 import de.schiemenz.stackcalc.math.*;
 
 public class Calculator {
 
 	private static Stack<Double> m_stack = new Stack<Double>();
 	private static Map<String, StackOperator> m_operators = new HashMap<String, StackOperator>();
 
 	/**
 		Main
 	*/
 	public static void main(String[] args) 
 	{
 		registerOperators();
 		printStack();
 		
 		Scanner scanner = new Scanner(System.in);
 		String next;
 		
 		while ((next = scanner.nextLine()) != null) 
 		{
 			if ("quit".equals(next)) 
 			{
 				break;
 			} 
 			else 
 			{
 				processInput(next);
 				printStack();
 			}
 		}
 		
 		System.exit(0);
 	}
 	/**
 		Parses user input and makes use of the stack operators accordingly
 	*/
 	@SuppressWarnings("unchecked")
 	public static void processInput(String input)
 	{	
 		if(m_operators.containsKey(input)) 
 		{	// registered operator found
 			Stack<Double> tmp = (Stack<Double>)m_stack.clone();
 			
 			try
 			{
 				m_stack = (m_operators.get(input)).getResult(m_stack);
 				//System.out.println("Parsed Operator: " + input);
 			}
 			catch(Exception e)
 			{
 				// the operator messed up - restoring stack
 				m_stack = tmp;
 			}
 		}
 		else
 		{	// a number or junk found
 			try 
 			{		
 				NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH); // e.g. 1.5 
 				double myNumber = nf.parse(input).doubleValue();
 				m_stack.push(myNumber);
 				//System.out.println("Parsed Operand: " + myNumber);
 			}
 			catch(NumberFormatException e){}
 			catch(ParseException e){}
 		}
 	}
 	/**
 		Registers the mathematical functions - 
 		can be used to include external stack operators
 	*/
 	public static void registerOperators()
 	{
 		m_operators.put(new StackAddition().toString(), new StackAddition());
 		m_operators.put(new StackSubtraction().toString(), new StackSubtraction());
 		m_operators.put(new StackMultiplication().toString(), new StackMultiplication());
 		m_operators.put(new StackDivision().toString(), new StackDivision());
 		m_operators.put(new StackPower().toString(), new StackPower());
 		m_operators.put(new StackSquare().toString(), new StackSquare());
 		m_operators.put(new StackSquareRoot().toString(), new StackSquareRoot());
 		m_operators.put(new StackSummation().toString(), new StackSummation());
 		m_operators.put(new StackArithmeticMean().toString(), new StackArithmeticMean());
 	}
 	/**
 		Returns the top element of the stack
 	*/
 	public static double getTop()
 	{
 		try 
 		{ 
 			return m_stack.peek();
 		}
 		catch(EmptyStackException e)
 		{
 			return 0.0d;
 		}
 	}
 	/**
 		Prints the stack to console
 	*/
 	private static void printStack()
 	{
		// TODO custom output
 		System.out.println("Stack " + m_stack.toString());	
 	}
 }
