 
 
 import java.io.*;
 import java.util.*;
 
 public class FileReader 
 {
 	FileReader(){
 		relation = "C:\\Users\\Jimmy\\Documents\\GitHub\\AMProgram\\Database";
 	}
 	
 	/*updateDatabase is an administrative accessed method. The administrator will use a file browser from the GUI
 	 * to locate a CSV file likely generated from eCampus. When the user finishes selecting a file
 	 * the file is sent here, where the reader will take the input, and then open an output
 	 * to overwrite a current file in the database*/
 	public void updateDatabase(String aFileName){
 		//open reader
 		//cut extraneous data
 		//output to database file
 	}
 	
 	
 	/*fetchCatalogues will be used to populate a list of classes for the user to choose from
 	 * while they are assembling their transcript. The results will also be used
 	 * by the CompareEngine when it makes recommendations for the user
 	 * The method is given a list of catalogs needed, and returns an array of lists*/
 	public String[][] fetchCatalog(String[] fields) throws IOException{
 		
 		String[][] catalogs = new String[fields.length][]; 
 	    Scanner scanner;
 	    int sizeOfFile;
 		
 		for(int i=0; i<fields.length; i++){
 			relation = (new StringBuilder(relation)).append("\\").append(fields[i]).append(".txt").toString(); //relation = C:\pathToDatabaseFolder\File, File determined by the value at fields[i]
 			scanner = new Scanner(new FileInputStream(relation)); //create a file reader targeting the ith catalog we need
 			
 			sizeOfFile = lengthOfTask(relation);
 			catalogs[i] = new String[sizeOfFile];
 			for(int j = 0; j<sizeOfFile; j++){
 				catalogs[i][j] = (new StringBuilder("")).append(scanner.nextLine()).append(NL).toString();
 				/*the above line will need tweaking once the data format is decided. as of now each line of the
 				 * array will store the data: "<original data from database>" */
 				//each "i" represents a catalog, and each "j" represents a line in the ith catalog
 			}
 			scanner.close(); //unlink the scanner for this catalog before moving onto the next
 		}		
 		
 		return catalogs;
 	}
 	
 	
 	/*fetchReqs is used to find out what requirements are needed for graduation for a specific degree
 	 * the method will be given the degree to be targeted and the reader will access that database file
 	 * in order to generate a list of requirements and how to satisfy those requirement. 
 	 * In the returned value, an array represents a requirement, and each element will contain a class
 	 * ID which satisfies it*/
 	public int[][] fetchReqs(String degree)throws IOException{
 		Scanner scanner;
 		int numOfReqs;
 		
 		relation = (new StringBuilder(relation)).append("\\").append(degree).append(".txt").toString(); //relation = C:\pathToDatabaseFolder\File
 		scanner = new Scanner(new FileInputStream(relation)); //create a file reader 
 			
 		numOfReqs = lengthOfTask(relation);
 		int[][] reqs = new int[numOfReqs][];
 		
 		for(int i = 0; i<numOfReqs; i++){
 			reqs[i] = new int[LengthOfLine(relation, i)];
 
 			for(int j = 0; j < reqs[i].length; j++){
 				StringBuilder newLine = new StringBuilder("");
 				newLine.append(scanner.next(","));
 				reqs[i][j] = Integer.parseInt(newLine.toString());
 				/*This reader is similar to the "fetchCatalogs" reader, but differs in that
 				 * instead of a catalog, the outer loop represents a requirement. each item of the inner
 				 * loop represents a class that fulfills that requirement.
 				 * This will need to be tweaked when database format is finalized, as currently
 				 * the first element is always the requirement ID, which may go unused later
 				 */
 			}
 			
 		}
 		scanner.close(); //unlink the scanner for this file
 				
 		return reqs;
 	}
 
 	
 	/*lengthOftask is used by the other methods to assess how many lines are in the file they will be
 	 * fetching data from, so that they will be able to properly initialize array sizes where
 	 * they will place that data*/
     private int lengthOfTask(String relation)
             throws FileNotFoundException
         {
             int taskLength = 0;
             Scanner lengthTest;
             for(lengthTest = new Scanner(new FileInputStream(relation)); lengthTest.hasNextLine();)
             {
                 lengthTest.nextLine();
                 taskLength++;
             }
 
             lengthTest.close();
             return taskLength;
         }
     
     private int LengthOfLine(String Relation, int lineNum)
     		throws FileNotFoundException
     {
     	int taskLength = 0;
         Scanner lengthTest;
        
         lengthTest = new Scanner(new FileInputStream(relation));
         for(int i = 0; i < lineNum; i++){
         	lengthTest.nextLine();
         }
         while(lengthTest.hasNext(",")){ //This is currently a source of issue. failing to recognize ","
         	lengthTest.next(",");		//this failure causes method to return line size of 0. no data copied
         	taskLength++;
         }
 
 
         lengthTest.close();
         return taskLength;
     }
     
     /*
      * This method fetches a 2D array of the login database information.
      * Each 'i' is a new student.
      * The first element of 'j' is the hashed-username.
      * The second element of 'j' is the hashed-password.
      */
 	public String[][] fetchLoginInfo() throws IOException{
 			
 		String relation2 = relation + "\\Login.txt"; //current directory, will be changed
 		Scanner scanner = new Scanner(new FileInputStream(relation2)); //create a file reader targeting the login database
 		String[][] loginCatalog = new String[lengthOfTask(relation2)][2];
 	    int sizeOfFile2 = lengthOfTask(relation2);
 	    loginInfoSize = sizeOfFile2; //Size of login info database
	    scanner.useDelimiter(",");
 		for(int i=0; i<sizeOfFile2; i++){
 			
 			//loginCatalog[i] = new String[sizeOfFile2];
 			for(int j = 0; j<2; j++){
				loginCatalog[i][j] = scanner.next(); //(new StringBuilder()).append(scanner.next(",")).toString();
 				/*the above line will take all the string info up to the next ',' */
 				//each "i" is a new student, the first "j" element is username, the second "j" element is the password
 			}
 		}		
 		scanner.close(); //unlink the scanner for this catalog before moving onto the next
 		return loginCatalog;
 	}
 	
 	/*
 	 * Returns the size of the login database file
 	 */
 	public int fetchLoginInfoSize(){
 		return loginInfoSize;
 	}
     
     private int loginInfoSize = 0; //Size of the login database
 	private static final String NL = System.getProperty("line.separator");
 	private String relation;
 }
