 import java.io.IOException;
 
 /**
  * @author Utkarsh Goel
  * @version 1.0.1 (1/7/2012)
  *	This is the main class and just provides the functionality of initialising other associated classes 
  */
 
 public class Gedcom2Xml
 {	
 	/*
 	 * Gets the filenames from the user
 	 * @param - accepts two parameters containg the names of input and output files
 	 */
 	public static void main(String[] args) throws IOException 
 	{
 		if (!(args.length == 2))
 		{
 			System.err.println("Input filename and output filename required.\ne.g. Royalty.txt Parsed.xml");
 			System.exit(1);
 		}
	    String inputFile = "../Data/"+args[0];
	    String outputFile = "../Result/"+args[1];
 	    FileRead fr = new FileRead(inputFile);
 	    FileWrite fw = new FileWrite(outputFile);
 	    @SuppressWarnings("unused")
 		Parser ps = new Parser(fr, fw);
 	    
 	    System.out.println("Success");
 	    System.exit(0);
 	}
 }
