 /**
  * 
  */
 package model;
 
 import java.io.*;
 import java.util.Date;
 import java.text.*;
 
 /**
  * @author Jeyaprakash Rajagopal, 
  * 		   Marc Wollenweber
  *
  */
 public class Logging {
 	
 	boolean exists = false;
 	static String filename;
 	private static Logging instance = null;
 	File file;	
 	BufferedWriter output;
 	DateFormat dateformat;
 	Date date;
 	
 	protected Logging() {
 		file=new File(filename);
 		exists = file.exists();
 		try {
 			if(!exists) {			
 				file.createNewFile();
 		    }
 			else {
 		    	output = new BufferedWriter(new FileWriter(file));
 				output.write("");
 				output.close();
 		    }	
 		}
 		catch(IOException e) {}
 	}
 	
 	public static Logging getInstance(String fileName) {
 		filename = fileName;
 	    if(instance == null) {
 		         instance = new Logging();
 		}
 		return instance;
 	}
 	
     public void LoggingFile(String logIdentifier, String args) {
    	dateformat = new SimpleDateFormat("HH:mm:ss");
		date = new Date();
 		try {
 			output = new BufferedWriter(new FileWriter(file,true));
 			output.write("[" + dateformat.format(date.getTime()) + "] " + logIdentifier + ": " + args + "\n");
 			output.close();
 		}
 		catch(IOException e) {}
     }
 }
