 import java.io.*;
 import java.util.Scanner;
 import java.util.NoSuchElementException;
 
 /**
  * Apache-login parsija
  */
 public class Splitter {
     
     public static void main(String args[]) throws Exception {
 	for (String filename: args) {
 	    Scanner scanner = new Scanner(new File(filename), "UTF-8");
 	    
 	    try {
		
		String line = scanner.nextLine();
		LogLine entry = new LogLine(line);
		System.out.println(entry.engineerDebug());
		
 	    } catch (NoSuchElementException foo) {
 		// Tiedosto kaiketi loppu, kaikki ok.
 	    } finally {
 		scanner.close();
 	    }
 	}
     }
 }
