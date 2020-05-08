 /**
  * 
  */
 package reindent;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 /**
  * @author Taylor Countryman <countryman.taylor@gmail.com>
  */
 public class Reindent
 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		// Argument checking 
 		if(args.length != 2)
 		{
 			System.err.println("Usage: java reindent.Reindent INFILE OUTFILE");
 			System.exit(1);
 		}
 		
 		// Input file checking 
 		File inFile = new File(args[0]);
 		if(!inFile.isFile() || !inFile.canRead() || inFile.length() <= 0) 
 		{
 			System.err.println( "reindent: " + inFile.getName() + ": Must be a readable file with content");
 			System.exit(1);
 		}
 		
 		// Setup scanner
 		Scanner inScanner = null;
 		try
 		{
 			inScanner = new Scanner(inFile);
 		}
 		catch(FileNotFoundException e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
		finally
		{
			if(inScanner != null)
				inScanner.close();
		}
 		
 		// Read file line by line
 		String line;
 		while(inScanner.hasNextLine())
 		{
 			line = inScanner.nextLine();
 			System.out.println(line);
 		}
 		
 	}
 }
