 package com.gooddata.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Category;
 
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.CSVWriter;
 
 public class CsvUtil {
 	
 	private static final char SEPARATOR = ',';
 	private static final char QUOTE = '"';
 	private static final char ESCAPE = '"';
 	
 	private static final String ENCODING = "utf-8";
	
	private static Category LOG = Category.getInstance(CsvUtil.class);
 
 	/**
 	 * Write the selected fields in defined order from the input stream CSV to the output
 	 * @param is CSV input
 	 * @param os CSV output
 	 * @param outputFields ordered list of output columns
 	 * @throws IOException if anything wrong happens with IO or CSV parsing/writing
 	 * @throws IllegalStateException if an expected field is missing in the source stream
 	 */
 	public static void reshuffle(final InputStream is, final OutputStream os, final List<String> outputFields) throws IOException {
 		final CSVReader reader = new CSVReader(new InputStreamReader(is, ENCODING), SEPARATOR, QUOTE, ESCAPE, 0);
 		final CSVWriter writer = new CSVWriter(new OutputStreamWriter(os, ENCODING), SEPARATOR, QUOTE, ESCAPE);
 		try {		
 			writer.writeNext(outputFields.toArray(new String[]{}));
 		    final String [] header = reader.readNext();
 		    if (header == null) 
 		    	return;
 	    	final Map<String,Integer> key2index = new HashMap<String,Integer>();
 	    	for (int i = 0; i < header.length; i++) {
 	    		key2index.put(header[i], i);
 	    	}
 		    
 	    	int lineNo = 0;
 		    String[] nextLine = null;
 		    try {
 			    Integer index;
 			    lineNo++;
 			    while ((nextLine = reader.readNext()) != null) {
 			    	if (nextLine.length < outputFields.size()) {
			    		LOG.warn("Line #" + lineNo + " contains " + nextLine.length + " fields only, " + outputFields.size() + " expected (" + outputFields + ").");
 			    		continue;
 			    	}
 			    	List<String> out = new ArrayList<String>();
 			        for (final String f : outputFields) {
 			        	index = key2index.get(f);
 			        	if (index == null) {
 			        		throw new IllegalStateException("Expected field " + f + " not found in the source CSV.");
 			        	}
 			        	out.add(nextLine[index]);
 			        }
 			        writer.writeNext(out.toArray(new String[]{}));
 			    }
 		    } catch (Exception e) {
 		    	throw new RuntimeException(e); // debug only
 		    }
 		} finally {
 			reader.close();
 			writer.close();
 		}
 	}
 	
 }
