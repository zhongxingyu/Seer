 package org;
 
 import java.lang.reflect.Method;
 import java.io.File;
 import java.io.Reader;
 import java.io.Writer;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.OutputStream;
 import java.io.FileOutputStream;
 
 public class TestApp {
 	private static void usage()
 	{
 		System.err.println("Usage: TestApp <algorithm> <input file> [-o <output file>]");
 	}
 
 	public static void main(String [] args) throws Throwable {
 		
 		File[] files;
 		File file = new File(args[1]);
 		
 		if (args.length < 2) {
 			usage();
 			return;
 		}
 
		Class stemClass = Class.forName("org.tartarus.snowball.ext.norwegianStemmer");
 		SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
 
 		Reader reader;
 		if(file.isDirectory()){
 			files = file.listFiles();
 			System.out.println(files.length);
 			for (int i = 0; i < files.length; i++) {
 				
 				System.out.println(files[i].getCanonicalPath());
 				reader = new InputStreamReader(new FileInputStream(files[i].getCanonicalPath()));
 				reader = new BufferedReader(reader);
 				
 				StringBuffer input = new StringBuffer();
 				
 				OutputStream outstream;
 				
 				if (args.length > 2) {
 					if (args.length >= 4 && args[2].equals("-o")) {
 						outstream = new FileOutputStream(args[3]+files[i].getName());
 					} else {
 						usage();
 						return;
 					}
 				} else {
 					outstream = System.out;
 				}
 				Writer output = new OutputStreamWriter(outstream);
 				output = new BufferedWriter(output);
 				
 				int repeat = 1;
 				if (args.length > 4) {
 					repeat = Integer.parseInt(args[4]);
 				}
 				
 				Object [] emptyArgs = new Object[0];
 				int character;
 				while ((character = reader.read()) != -1) {
 					char ch = (char) character;
 					if (Character.isWhitespace((char) ch)) {
 						if (input.length() > 0) {
 							stemmer.setCurrent(input.toString());
 							for (int i1 = repeat; i1 != 0; i1--) {
 								stemmer.stem();
 							}
 							output.write(stemmer.getCurrent());
 							output.write('\n');
 							input.delete(0, input.length());
 						}
 					} else {
 						input.append(Character.toLowerCase(ch));
 					}
 				}
 				
 				output.flush();
 			}
 		}
 	}
 }
