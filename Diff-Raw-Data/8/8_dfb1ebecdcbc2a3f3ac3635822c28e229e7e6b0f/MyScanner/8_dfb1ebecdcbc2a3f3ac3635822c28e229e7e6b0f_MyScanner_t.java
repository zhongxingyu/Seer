 package ca2;
 
 import java.util.InputMismatchException;
 import java.util.Scanner;
 public class MyScanner 
 {
 	private Scanner kb;
 	public MyScanner()
 	{
 		this.kb = new Scanner(System.in);
 	}
 	
 	//e.g. use for mobile phone numbers - 086 1234567
 	/**
 	 * Returns a user-entered value if the value conforms to a Regular Expression.
 	 *<p>
 	 *For example, we can ask the user for a mobile phone number and, with the appropriate Regex, determine if the string entered is in the correct format. 
 	 *
 	 * @param strPrompt String prompt to indicate what you wish the user to enter from the command line. 
 	 * @param strRegex  Regular Expression against which the user-entered string value will be matched.
 	 * @return String entered by the user if it conforms to the regular expression.
 	 */
 	public String validate(String strPrompt, String strRegex)
 	{
 		//e.g. 086 1234567 or 0867777777 or 086   3456788
 		boolean bContinue = true;
 		String strIn = "";
 		while(bContinue)
 		{
 			System.out.println(strPrompt);
 			strIn = kb.nextLine();
 			
 			if(strIn.matches(strRegex))
 			{
 				bContinue = false;
 			}
 			else
 			{
 				System.out.println("Invalid string based on Regex: " + strRegex);
 			}
 		}
 		return strIn;
 	}
 	
 	/**
 	 * Returns a string in the length range minLength-maxLength. Repeats if the value entered is invalid.
 	 * 
 	 * @param strPrompt String prompt to indicate what you wish the user to enter from the command line. 
 	 * @param minLength Minimum acceptable length for the input string.
 	 * @param maxLength Maximum acceptable length for the input string.
 	 * @return String entered by the user if it conforms to length criteria specified.
 	 */
 	public String nextLine(String strPrompt, int minLength, int maxLength)
 	{
                kb.nextLine();
 		String strIn = "";
 		int strlength = 0;
 		boolean bContinue = true;
 		while(bContinue)
 		{
 			System.out.println(strPrompt);
 			strIn = kb.nextLine();
 			strlength = strIn.length();
 			if((strlength >= minLength) && (strlength <= maxLength))
 			{
 				bContinue = false;
 			}
 			else
 			{
				System.out.println("Invalid string length: Range is " + minLength + " - " + maxLength);
 			}
 		}
 		
 		return strIn;
 		
 	}
 		
 	/**
 	 * Returns an integer value in the range lo-hi. Repeats if the value entered is invalid.
 	 * 
 	 * @param strPrompt String prompt to indicate what you wish the user to enter from the command line. 
 	 * @param lo Minimum acceptable value for the input string.
 	 * @param hi Maximum acceptable value for the input string.
 	 * @return Integer value entered by the user if it conforms to the range criteria specified.
 	 */
 	public int nextInt(String strPrompt, int lo, int hi)
 	{
 		int value = 0;
 		boolean bContinue = true;
 		while(bContinue)
 		{	
 			value = nextInt(strPrompt);
 			if((value >= lo) && (value <= hi))
 			{
 				bContinue = false;
 			}
 			else
 			{
 				System.out.println("Invalid length: Range is " 
 									+ lo + " - " + hi);
 			}
 		}
                
 		return value;
 	}	
 	
 	/**
 	 * Returns a double value in the range lo-hi. Repeats if the value entered is invalid.
 	 * 
 	 * @param strPrompt String prompt to indicate what you wish the user to enter from the command line. 
 	 * @param lo Minimum acceptable value for the input string.
 	 * @param hi Maximum acceptable value for the input string.
 	 * @return Double value entered by the user if it conforms to the range criteria specified.
 	 */
 	public double nextDouble(String strPrompt, double lo, double hi)
 	{
 		double value = 0;
 		boolean bContinue = true;
 		while(bContinue)
 		{	
 			value = nextDouble(strPrompt);
 			if((value >= lo) && (value <= hi))
 			{
 				bContinue = false;
 			}
 			else
 			{
 				System.out.println("Invalid length: Range is " 
 									+ lo + " - " + hi);
 			}
 		}
 		
 		return value;
 	}
 		
 	/**
 	 * Returns a double value. Repeats if the value entered is invalid.
 	 * @param strPrompt String prompt to indicate what you wish the user to enter from the command line. 
 	 * @return Double value entered by the user.
 	 */
 	public double nextDouble(String strPrompt)
 	{
 		double value = 0;
 		boolean bContinue = true;
 	
 		while(bContinue)
 		{
 			try
 			{
 				System.out.println(strPrompt);
 				value = kb.nextDouble();				
 				bContinue = false;
 			}
 			catch(InputMismatchException e)
 			{
 				kb.nextLine();
 				System.out.println("Invalid format!");			
 			}
 		}	
 		//if i get to here i have a valid number
 		return value;
 	}
 	
 	/**
 	 * Returns an integer value. Repeats if the value entered is invalid.
 	 * @param strPrompt String prompt to indicate what you wish the user to enter from the command line. 
 	 * @return Integer value entered by the user.
 	 */
 	
 	public int nextInt(String strPrompt)
 	{
 		int value = 0;
 		boolean bContinue = true;
 	
 		while(bContinue)
 		{
 			try
 			{
 				System.out.println(strPrompt);
 				value = kb.nextInt();				
 				bContinue = false;
 			}
 			catch(InputMismatchException e)
 			{
 				kb.nextLine();
 				System.out.println("Invalid format!");			
 			}
 		}	
 		//if i get to here i have a valid number
 		return value;
 	}
 }
