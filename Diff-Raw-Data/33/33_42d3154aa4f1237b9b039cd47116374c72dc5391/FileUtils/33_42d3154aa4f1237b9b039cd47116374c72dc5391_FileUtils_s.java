 package org.layr.commons;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 
 public class FileUtils {
 
 	public static String readFileAsString(String fileName)
 			throws IOException {
 		InputStream stream = getResourceAsStream(fileName);
 	    try {
 	        return readFileAsString(stream);
 	    } finally {
 	    	if ( stream != null )
 	    		stream.close();
 	    }
 	}
 
 	public static InputStream getResourceAsStream(String fileName)
 			throws FileNotFoundException {
 		return Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
 	}
 
 	public static String readFileAsString(InputStream stream)
 			throws UnsupportedEncodingException, IOException {
 		Reader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
 		StringBuilder builder = new StringBuilder();
 		char[] buffer = new char[8192];
 		int read;
 		while ((read = reader.read(buffer, 0, buffer.length)) > 0)
 		    builder.append(buffer, 0, read);
 		return builder.toString();
 	}
 
 }
