 package br.ufmg.dcc.vod.ncrawler.common;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.zip.GZIPOutputStream;
 
 /**
  * Utilities for dealing with files. 
  */
 public class FileUtil {
 
 	public static List<String> readFileToList(File file) throws IOException	{
	    new Object();
	    
 		LinkedList<String> queue = new LinkedList<String>();
 		BufferedReader br = null;
 		String line;
 		
 		try	{
 			br = new BufferedReader(new FileReader(file));
 			while((line = br.readLine()) != null) {
 				queue.add(line);
 			}
 			
 			br.close();
 		} finally {
 			if(br != null) {
 				br.close();
 			}
 		}
 		return queue;
 	}
 	
 	public static void saveUrlGzip(InputStream inputStream, File filePath) throws IOException {
 	    BufferedReader in = null;
 	    PrintStream out = null;
 	    try 
 	    {
 		    in = new BufferedReader(new InputStreamReader(inputStream));
 			out = new PrintStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(filePath))));
 		    
 		    String inputLine;
 		    while ((inputLine = in.readLine()) != null) {
 		    	out.println(inputLine);
 		    }
 		    
 		    out.flush();
 		    out.close();
 	    }
 	    finally
 	    {
 	    	if (in != null) {
 				in.close();
 	    	}
 	    	if (out != null) {
 	    		out.close();
 	    	}
 	    }
 	}
 
 	public static LinkedHashSet<String> readFileToSet(File file) throws IOException {
 		LinkedHashSet<String> queue = new LinkedHashSet<String>();
 		BufferedReader br = null;
 		String line;
 		
 		try	{
 			br = new BufferedReader(new FileReader(file));
 			while((line = br.readLine()) != null) {
 				queue.add(line);
 			}
 			
 			br.close();
 		} finally {
 			if(br != null) {
 				br.close();
 			}
 		}
 		return queue;
 	}
 	
 }
