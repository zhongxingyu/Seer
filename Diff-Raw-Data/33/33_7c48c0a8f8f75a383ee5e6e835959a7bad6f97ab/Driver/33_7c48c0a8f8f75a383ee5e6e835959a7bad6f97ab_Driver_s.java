 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.swing.JOptionPane;
 
 
 /**
  * Project #2
  * CS 2334, Section 010
  * Oct 1, 2013
  * <P>
  * This class will allow you to read a file of publication information, search through the data, and write data onto a file.
  * </P>
  *@version 1.0
  */
 public class Driver {
 	
	public static void main (String[] args) {
 		
 		JOptionPane.showMessageDialog(null, "Welcome to the Publication Searcher. Please click OK to continue in the console.");
 		
 		//Scanner for user input data
 		Scanner in = new Scanner(System.in);
 		
 		//For loop for repeating search
 		for(int i=0; i==0;){
 		
 		//Ask for and read in the file
 		System.out.println("Please enter the name of the file");
 		String filename = in.nextLine();
 		Parser p = new Parser(filename);		
 		
 		//For loop for repeating search
 		for(int j=0; j==0;){
 		
 		/*
 		 * Not needed, but may want to use.
 		//Would you like to search journal articles, conference papers, or both?
 		System.out.println("Would you like to search journal articles, conference papers, or both?");
 		System.out.println("1. Journal Articles");
 		System.out.println("2. Conference Papers");
 		System.out.println("3. Both");	
 		
 		//Save input as the choice
 		String choice = in.nextLine();
 		int num1 = Integer.valueOf(choice);
 		*/
 			
 		//Text-based user interface for searching the magazines
 		System.out.println("Choose a criteria to serach by entering the coressponding digit:");
 		System.out.println("1. Name of Author");
 		System.out.println("2. Name of Paper/Article");
 		System.out.println("3. Name of the Journal/Collection of Paper");
 		System.out.println("4. Exit the Program");
 			
 		//Save input as the choice
 		String choice = in.nextLine();
 		int num = Integer.valueOf(choice);
 
 		//Manages choice using if statements
 		if(num==1){System.out.println("You are searching by Name of Author. Please enter a name to search for.");}
 		else if(num==2){System.out.println("You are searching by Name of Article/Paper. Please enter a name to search for.");}
 		else if(num==3){System.out.println("You are searching by Name of Journal/Collection of Papers. Please enter a name to search for.");}
 		else if(num==4){i=1; System.exit(-1);}
 		else System.out.println("Please enter a valid option number.");
 		
 		//Search for data here using specified criteria type
 		String search = in.nextLine();
 		String results = "";
 		//the results of the search
 		Boolean resultsBool = false;
 		
 		//ArrayList of Publications made from Parser
 		ArrayList<Publication> pubList = new ArrayList<Publication>();
 		pubList = p.getPublications();
 		
 		//Search through magazines. Check that all magazines are stored into one large arraylist
 		if(num==1){
 			for(Publication pub : pubList){
				if(pub.getAuthors() != null && pub.getAuthors().contains(search)){results = pub.toString();resultsBool = true;}
 				else {resultsBool = false;}
 		}}
 		else if(num==2){
 			for(Publication pub : pubList){
				if(pub.getTitlePaper() != null && pub.getTitlePaper().contains(search)){results = pub.toString();resultsBool = true;}
 				else {resultsBool = false;}
 		}}
 		else if(num==3){
 			for(Publication pub : pubList){
				if(pub.getTitleSerial() != null && pub.getTitleSerial().contains(search)){results = pub.toString();resultsBool = true;}
 				else {resultsBool = false;}
 		}}
 		
 		//JOptionPane that displays search results
 		if(resultsBool==true) JOptionPane.showMessageDialog(null, results);
		else if(resultsBool==false) JOptionPane.showMessageDialog(null, "There were no magazines with that matched your search.");
 		
 		//Yes and No JOptionPane used to continue searching or quit program
 		if (JOptionPane.showConfirmDialog(null, "Do you wish to search again?", "WARNING", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 		    //Yes option
 			j=0;
 		} else {
 			//No option
 			j=1;
 			in.close();
 			System.exit(-1);
 		}
 		
 		}
 	
 		
 		//Yes and No JOptionPane used to search using a different file or quit program
 		if (JOptionPane.showConfirmDialog(null, "Do you wish to search again?", "WARNING", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 		    //Yes option
 			i=0;
 		} else {
 			//No option
 			i=1;
 			in.close();
 			System.exit(-1);
 		}
 		
 		}
 	}
 
 	/**
 	 * Retrieves user input for file location & search term
 	 * 
 	 * @return input   user input
 	 */
 	public String getInput() {
 		
 		Scanner in = new Scanner(System.in);
 		String input = in.nextLine();
 		in.close();
 		
 		/**
 		 * TODO: not used
 		 */
 		
 		return input;
 	}
 }
