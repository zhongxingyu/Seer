 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 
 
 public class PaperCollection {
 	
 	private ArrayList<Paper> paperCollection = new ArrayList<Paper>();
 	
 	/**
 	 * Default constructor for the class.
 	 */
 	public PaperCollection() {}
 	
 	/**
 	 * Preferred constructor for the class.
 	 * Reads in a text file and constructs the proper paper based on the information given.
 	 * @param filepath File path of the text file that contains the list of papers and their details.
 	 * @throws IOException Thrown if a readLine error occurs.
 	 */
 	public PaperCollection(String filepath) throws IOException
 	{
 		FileReader fr = null;
 		String line = "";

 		try {
 			fr = new FileReader(filepath);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		BufferedReader br = new BufferedReader(fr);
 
		//Initial line grab
		line = br.readLine(); 
		
		//Holds the info for the current paper. Will be used in the construction of a new paper object.
		String[] paperInfo = new String[7]; 
 		int i = 0;
 
		//Until the end of the file
		while (line != null) {
			//Loops until a new magazine is found.
			do { 
				paperInfo[i++] = line;
 				line = br.readLine();
				
				//End of file, break from the while loop since it's difficult to conditional this.
				if (line == null) 
 					break;
 			} while (!(line.equalsIgnoreCase(""))); //Detection of a new paper.
			
			//Create the appropriate constructor
			if (paperInfo[0].equalsIgnoreCase("Journal Article")) 
 				paperCollection.add(new JournalArticle(paperInfo[0], paperInfo[1], paperInfo[2], paperInfo[3], paperInfo[4], paperInfo[5], paperInfo[6]));
 			else if (paperInfo[0].equalsIgnoreCase("Conference Paper"))
 				paperCollection.add(new ConferencePaper(paperInfo[0], paperInfo[1], paperInfo[2], paperInfo[3], paperInfo[4], paperInfo[5], paperInfo[6]));
			else System.out.println("Improper Formatting Detected - \"Conference Paper\" or \"Journal Article\" expected."); //Should never be reached or we have a problem.
			
			
 			paperInfo = new String[7]; //Reset the information arrays
 			i = 0;
 			line = br.readLine();
 		}
 	}
 	
 	/**
 	 * Sorts the collection by a certain criteria.
 	 * 
 	 * May require a few smaller methods, but we'll see in implementation.
 	 * @param method Which element to sort by (ex. BI for bibliographic, AU for author, etc.)
 	 */
 	public void Sort(String method)
 	{
 		Paper.setSortSearchCriteria(method.toUpperCase());
 		if (method.equalsIgnoreCase("R"))
 		{
 			long seed = System.nanoTime();
 			Collections.shuffle(paperCollection, new Random(seed));
 		}
 		else
 			Collections.sort(paperCollection);
 
 		for(Paper eachPaper:paperCollection)
 			System.out.println(eachPaper);
 		//Switch to choose how to sort (which criteria)
 		//Methods for individual sorts?
 		//Maybe a .toString on the paper and then sort by a selected index in a split up array.
 		
 	}
 	
 	/**
 	 * Prints the data in the collection to a file on the drive
 	 * @param filepath Where you want to print the file to
 	 * @throws IOException Error in reading the file, in our out.
 	 */
 	public void printToFile(String filepath) throws IOException
 	{
 		//Make a file to print to and open a buffer
 		FileWriter outputFile = new FileWriter(filepath);
 		BufferedWriter bw = new BufferedWriter(outputFile);
 		
 		//Go through each paper in the collection and print it to the file
 		for(Paper eachPaper: paperCollection){
 			bw.write(eachPaper.toString().replace(" // null", "").replace(" // ", "\n"));
 			bw.newLine();
 			bw.newLine();
 		}
 		
 		//Close the buffer
 		bw.close();
 	}
 	
 	/**
 	 * Prints the data in the collection to the screen for the user to view
 	 */
 	public void printToScreen()
 	{
 		for(Paper eachPaper:paperCollection)
 				System.out.println(eachPaper.toString().replace(" // null", "").replace(" // ", "\n") + "\n");
 		//Same as file, but to screen, not a file.
 	}
 	
 	/**
 	 * Searches the collection for a keyword.
 	 * @param searchCriteria The search query
 	 * @return A string containing the results of the search
 	 */
 	public String search(String searchCriteria)
 	{
 		return "";
 	}
 
 	/**
 	 * Gets the size of the list that contains the papers.
 	 * @return The size of the arrayList that the papers are stored in.
 	 */
 	public int size() {
 		return paperCollection.size();
 	}
 }
