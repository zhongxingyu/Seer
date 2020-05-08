 import java.io.*;
 
 public class QMMMain 
 {
 	public static void main(String[] args) 
 	{
 		// TODO Read in variables
 		//		Sort and organize
 		//		Analyze
 		//		Minimize
 		
 		// Print out greeting
 		System.out.print("Enter the number of inputs: ");
 		int maxNum = 0;
 		
 		//Try to read in the number of inputs;
 		try
 		{
			BufferedReader bfRead = new BufferedReader(new InputStreamReader(System.in));
			String inputs = bfRead.readLine();
			maxNum = Integer.parseInt(inputs);
 		}
 			catch (NumberFormatException ex)
 		{
 			System.err.println("Not a valid number: " + maxNum);
 		}
 			catch (IOException e)
 		{
 			System.err.println("Unexpected IO Error: " + e);
 		}
 		
 		//Create int array and read in ints
 		int minTerms[];
 		minTerms = gatherTerms();
 		
 		
 		//Convert ints into a bin array
 		
 		
 	}
 	
 	public static int[] gatherTerms()
 	{
 		int minReturn[] = new int[0];
 		int i = 0;
 		//use string type to hold input vars
 		System.out.println("Please enter the min terms, seperated by a space");
 		String minTerms = null;
 		String tokens[];
 		try
 		{
 			BufferedReader bfRead = new BufferedReader(new InputStreamReader(System.in));
 			minTerms = bfRead.readLine();
 			tokens = minTerms.split("[ ]");
 			
 			minReturn = new int[tokens.length];
 			for(i = 0; i < tokens.length; i++)
 			{
 				minReturn[i] = Integer.parseInt(tokens[i]);
 			}
 		} 
 			catch (NumberFormatException ex)
 		{
 			System.err.println("Not a valid number: " + minTerms);
 		}
 			catch (IOException e)
 		{
 			System.err.println("Unexpected IO Error: " + e);
 		}
 		
 		//Echo inputs
 		for(i = 0; i < minReturn.length; i++)
 		{
 			System.out.print(minReturn[i] + " ");
 		}
 		
 		return minReturn;
 	}
 	
 
 }
