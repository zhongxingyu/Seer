 //Computer Science 421 - Artificial Intelligence
 //University of Victoria
 //David Williams - V00701616
 
 /*
  This creates a DataRead object, which contains all the data in a correctly formatted file. The file can be of any type, but must contain the data in the format:
  arg , arg , arg , ... , arg , class/n
  Whitespace between arguments is optional.
  
  Usage: Create a new dataread object, passing as an argument the name of the file to be read.
  
  Contains the following data:
  DataRead.linecount -> Number of lines of data
  DataRead.argcount -> Number of arguments (does not include the class)
  DataRead.storage[i][j] -> Stores the arguments. i = line, j = argument in that line
  DataRead.classes[i] -> Classes for each line i.
 */
 
 
 import java.io.*;
 
 public class DataRead {
 	static int MAXSIZE = 4000;
 	int linecount;
 	int argcount;
 	int numunique;
 	double[][] storage;
 	String[] classes;
 	String[] classlist;
 	
 	public DataRead(){			//Just incase you call it wrong
 		linecount = 0;
 		argcount = 0;
 		storage = null;
 		classes = null;
 	}
 	
 	public DataRead(String arg){
 		int count = 0;					//A few temporary variables
 		String strLine;
 		String[] strSplit;
 		System.out.println("Opening File");
 		BufferedReader br = OpenFile(arg);			//Get the reader for reading the data. Same reader is used to find argcount.
 		BufferedReader counter = OpenFile(arg);		//Temporary reader just to determine the filesize
 		argcount = CountLength(br) - 1;
 		linecount = CountLines(counter);
 		System.out.println("Preparing Buffer");
 		storage = new double[linecount][argcount];	//Make storage big enough for the file
 		classes = new String[linecount];
 		System.out.println("Buffering");
 		try {
 			while ((strLine = br.readLine()) != null){
 				strSplit = strLine.split("\\s*,\\s*");	//Split on commas surrounded with whitespace
 				for (int i = 0; i < argcount; i++){
 					storage[count][i] = Double.parseDouble(strSplit[i]);
 				}
 				classes[count] = strSplit[argcount];
 				count++;
 			}
 		}
 		catch (Exception e){
 			System.err.println("Error: " + e.getMessage());
 		}
 		System.out.println("Buffering Complete");
 		System.out.println("Populating Class List");
 		classlist = PopulateClasses(classes, linecount);
 		System.out.println("Complete");
 	}
 	
 	public String[] PopulateClasses(String[] classes, int linecount){
 		numunique = 0;
 		boolean newclass = true;
 		String[] classlist = new String[linecount];
 		for (int i = 0; i < linecount; i++){
 			for (int j = 0; j < numunique; j++) {
				if (classes[i] == null || classes[i].equals(classlist[j])) {
 					newclass = false;
 				}
 			}
 			if (newclass == true) {
 				classlist[numunique] = classes[i];
 				numunique++;
 			}
 			newclass = true;
 		}
 		return classlist;
 	}
 	
 	public int CountLines(BufferedReader br){		//Counts how many lines of data there are in the file
 		int count = 0;
 		String strLine;
 		try {
 			while ((strLine = br.readLine()) != null){
 				count++;
 			}
 		}
 		catch (Exception e){
 			System.err.println("Error: " + e.getMessage());
 		}
 		return count;
 	}
 	
 	public int CountLength(BufferedReader br){		//Counts how long (how many arguments) are in one line of the file. Returns the reader to the top when done.
 		try {
 			br.mark(MAXSIZE);
 			String str = br.readLine();
 			String[] split = str.split("\\s*,\\s*");
 			br.reset();
 			return split.length;
 		}
 		catch (Exception e){
 			System.err.println("Error: " + e.getMessage());
 		}
 		return -1;
 	}
 	
 	public BufferedReader OpenFile(String blargh){		//Provides a BufferedReader for the file.
 		try {
 			FileInputStream fstream = new FileInputStream(blargh);
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			return br;
 		}
 		catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 		}
 		return null;
 	}
}
