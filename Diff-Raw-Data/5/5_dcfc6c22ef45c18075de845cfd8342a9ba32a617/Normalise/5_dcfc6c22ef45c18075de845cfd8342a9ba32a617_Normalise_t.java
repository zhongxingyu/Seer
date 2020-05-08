 /*
 * Copyright 2013 University of Edinburgh.
 *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
 *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package uk.org.ukfederation.mdnorm;
 
 import java.io.BufferedReader;
 import java.io.CharArrayWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 
 /**
  * Metadata file normalisation.
  * 
  * @author iay
  */
 public class Normalise implements Runnable {
 
 	private static final boolean retainOddSpaces = false;
 	private static final boolean retainBlankLines = true;
 	private static final int tabSize = 4;
 	
 	private final File file;
 
 	private static void croak(String s, Exception e) {
 		e.printStackTrace();
 		System.err.println("Internal error: " + s + ": " + e.getMessage());
 		System.exit(1);
 	}
 
 	private CharArrayWriter process(Reader in) throws IOException {
 		CharArrayWriter w = new CharArrayWriter();
 		int lead = 0; // amount of leading white space
 		boolean start = true; // processing the start of the line
 		int c;
 		for (;;) {
 			c = in.read();
 			
 			if (start) {
 				switch (c) {
 				
 				case -1:
 					// end of input at line start
 					return w;
 					
 				case '\r':
 					// drop carriage return if we see one
 					break;
 					
 				case ' ':
 					lead++;
 					break;
 					
 				case '\t':
 					do { lead++; } while ((lead % tabSize) != 0);
 					break;
 
 				case '\n':
 					// line contains only whitespace
 					lead = 0; // throw away white space entirely
 					if (retainBlankLines) w.append('\n');
 					break;
 				
 				default:
 					// flush leading space as tabs
 					while (lead >= tabSize) {
 						lead -= tabSize;
 						w.append('\t');
 					}
 					
 					/*
 					 * If the tabs don't make up the required space
 					 * exactly, optionally make up the difference with spaces.
 					 */
 					if (retainOddSpaces) {
 						while (lead > 0) {
 							lead--;
 							w.append(' ');
 						}
 					}
 					
 					// no longer processing leading space
 					start = false;
 					
 					// retain this character
 					w.append((char)c);
 					break;
 					
 				}
 				
 			} else {
 				
 				switch (c) {
 				
 				case -1:
 					// end of file in middle of line
 					w.append('\n');
 					return w;
 					
 				case '\n':
 					start = true;
 					lead = 0;
 				default:
 					w.append((char)c);
 					break;
 
 				}
 			}
 		}
 	}
 	
 	public void run() {
 		
 		/*
 		 * Open the input file and construct a Reader to
 		 * access it.
 		 */
 		InputStream is;
 		try {
 			is = new FileInputStream(file);
 		} catch (FileNotFoundException e) {
 			croak("input file not found", e);
 			return;
 		}
 		Reader in;
 		try {
 			in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			croak("UTF-8 not supported", e);
 			return;
 		}
 		
 		/*
 		 * Run through the input file processing each character in turn until we
 		 * get to the end.  The results are collected in a CharArrayWriter.
 		 */
 		CharArrayWriter w;
 		try {
 			w = process(in);
 		} catch (IOException e) {
 			croak("I/O exception while processing file", e);
 			return;
 		}
 		
 		/*
 		 * We're finished with the input file.
 		 */
 		try {
 			in.close();
 		} catch (IOException e) {
 			croak("can't close input file", e);
 			return;
 		}
 		
 		/*
 		 * Write the processed data back into the same file.
 		 */
 		OutputStream os;
 		try {
 			os = new FileOutputStream(file);
 		} catch (FileNotFoundException e) {
 			croak("output file not found", e);
 			return;
 		}
 		Writer out;
 		try {
 			out = new OutputStreamWriter(os, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			croak("UTF-8 not supported", e);
 			return;
 		}
 		try {
 			w.writeTo(out);
 			out.close();
 		} catch (IOException e) {
 			croak("I/O exception while writing output file", e);
 			return;
 		}
 	}
 	
 	/**
 	 * Constructor.
 	 */
 	private Normalise(File file) {
 		this.file = file;
 	}
 	
 	/**
 	 * Command-line entry point.
 	 */
 	public static void main(String[] args) {
 		/*
 		 * Parse command-line arguments.
 		 */
 		if (args.length != 1) {
 			System.err.println("Usage: Normalise <file>");
 			System.exit(1);
 		}
 		
 		Normalise norm = new Normalise(new File(args[0]));
 		norm.run();
 	}
 
 }
