 package wordCounter;
 /**
  * Word Counter
  * More comments later.
  */
 
 import java.io.*;
 import java.net.URL;
 
 import wordCounterTest.WCTest;
 
 // This is a meaningless test comment. -Kevin
 // This is a second meaningless comment
 //This is a third meaningless comment - Xiaofang
 
 /**
  * The main class for Word Counter.
  */
 public class WC {
 
 	// defaults
 	public static final int DEFAULT_THRESHOLD = 1;
 	public static final String DEFAULT_DELIMITERS = " .,:;";
 	
 	// static member variables.
 	public static FileReader fileInput;
 	public static int iThreshold = DEFAULT_THRESHOLD;
 	public static String strDelimiters = DEFAULT_DELIMITERS;
 	public static String fileName;
 	
 	// exit status codes.
 	public static final int ARGUMENT_ERROR = 1;
 	public static final int FILE_ERROR = 2;
 	public static final int IO_ERROR = 3;
 	
 	/**
 	 * @param args
 	 * @author KevinJones
 	 * 
 	 */
 	public static void main(String[] args) {
 		
 		init();
 		
 		if(!fetchArguments(args))
 		{
 			System.out.println("ERROR: ill-formed command");
 			System.exit(ARGUMENT_ERROR);
 		}
 		
 		if(!openFile())
 		{
 			System.out.println("ERROR: unable to process file");
 			System.exit(FILE_ERROR);
 		}
 		
 		int wordsCounted = 0;
 		try {
 			wordsCounted = countWords();
 		} catch (IOException e) {
 			System.out.println("ERROR: File IO exception");
 			System.exit(IO_ERROR);
 		}
 		System.out.println(wordsCounted);
 	}
 	
 	public static Boolean init()
 	{
 		//TODO: implement this method
 		return true;
 	}
 	
 	public static Boolean fetchArguments(String[] args)
 	{
 		/*Initialize reuired variables*/
 		int args_length = args.length;
 		int min_word_count = 1;
 		String delimiters = new String();
 		String[] copy_args =  new String[args_length];
 		
 		/*Extract the command-line arguments*/
 				
 		for (int i = 0; i<args_length; i++){
 			copy_args[i] = args[i];
 		}
 		
 		/*Extract the minimum word count or threshold and the delimiters*/
 		for (int i=0; i<args.length; i++){
 			if (copy_args[i].equals("-l")){
 				min_word_count = Integer.parseInt(copy_args[i+1]);
 			}				
 			if (copy_args[i].equals("-c")){
 				delimiters = copy_args[i+1];
 			}							
 		}
 		
 		try{
 		fileName = copy_args[0];
 		}catch(ArrayIndexOutOfBoundsException e){
 			return false;
 		}
 		
 		iThreshold = min_word_count;
 		strDelimiters = delimiters;
 		return true;
 	}
 	
 	public static Boolean openFile()
 	{
 		Class<WCTest> c = WCTest.class;
 		ClassLoader cl = c.getClassLoader();
 		URL url = cl.getResource(fileName);
		if(url == null)
		{
			return false;
		}
 		String fullPath = url.getPath();
 		String replacedPath = fullPath.replaceAll("%20", " ");
 		
 		FileReader r = null;
 		try {
 			r = new FileReader(replacedPath);
 		} catch (FileNotFoundException e1) {
 			return false;
 		}
 		
 		fileInput = r;
 		
 		return true;
 	}
 	
 	/**
 	 * Reads the inputFile, delimiters, and wordThreshold variables to count the
 	 * number of words (sequences of non-delimiter characters between two delimiters)
 	 * in the input file.
 	 * @return The number of words in the file
 	 * @throws IOException if an I/O operation fails or is interrupted
 	 */
 	public static int countWords() throws IOException
 	{
         int wordLength = 0;
         int wordCount = 0;
         boolean[] delimiterFlags = {false, false};
 
         int readInt = fileInput.read();
         boolean isEndOfFile = (readInt == -1);
         char c = (char) readInt;
         while(!isEndOfFile && wordLength < Integer.MAX_VALUE && wordCount < Integer.MAX_VALUE)
         {
             wordLength++;
             boolean charIsDelimiter = strDelimiters.indexOf(c) != -1;
             if(charIsDelimiter)
             {
                 delimiterFlags[1] = true;
                 boolean hasDelimitersOnBothSides = (delimiterFlags[0] && delimiterFlags[1]);
                 if (hasDelimitersOnBothSides)
                 {
                     if(wordLength - 1 >= iThreshold)
                     {
                         wordCount++;
                     }
                 }
                 wordLength = 0;
                 delimiterFlags[0] = true;
                 delimiterFlags[1] = false;
             }
             
             // read the next char.
             readInt = fileInput.read();
             isEndOfFile = (readInt == -1);
             c = (char) readInt;
 
         }
 
         return wordCount;
 	}
 
 }
