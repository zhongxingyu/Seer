 package utils;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 
 public class TextConvertor {
 
 		private File textFile;
 		public ArrayList<Person> personList;
 		
 		public TextConvertor(){		
 		}
 			
 		public TextConvertor(File importedFile){
 			
 			textFile = importedFile;
 			personList = new ArrayList<Person>();
 	        
 	        try {
 	           
 	            Scanner scanner = new Scanner(textFile);
 	            
 	            while (scanner.hasNextLine()) {
 	                
 	            	String line = scanner.nextLine();
 	                
 	            	Scanner splitText = new Scanner(line).useDelimiter("\\t");
 	            	            	
 	            	String naam = splitText.next();
 	            	String datum = splitText.next();
 	            		          
 	            	splitText.close();
 	            	
 	            	Person persoon = new Person(naam, datum);
 	            	
 	            	personList.add(persoon);                	             
 	            }
 	            
 	            scanner.close();
 	            
 	        } 
 	        catch (FileNotFoundException e) {
 	            e.printStackTrace();
 	        }      
 			
 		}
 
 }
