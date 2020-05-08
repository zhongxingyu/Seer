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
	    int linenum = 1;
	    String line = "";
 	    
 	    try {
		while (true) {
		    line = scanner.nextLine();
		    LogLine entry = new LogLine(line);
		    System.out.println(entry.engineerDebug());
		    linenum++;
		}
 	    } catch (NoSuchElementException foo) {
 		// Tiedosto kaiketi loppu, kaikki ok.
	    } catch (Exception e) {
		System.err.println("Stopped to line "+linenum);
		System.err.println("Content: "+line);
		throw e;
 	    } finally {
 		scanner.close();
 	    }
 	}
     }
 }
