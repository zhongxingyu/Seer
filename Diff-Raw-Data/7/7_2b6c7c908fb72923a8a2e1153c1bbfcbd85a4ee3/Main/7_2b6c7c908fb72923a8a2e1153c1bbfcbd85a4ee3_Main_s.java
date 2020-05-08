 package org.lesscss.cli;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import org.lesscss.LessCompiler;
 import org.lesscss.LessException;
 
 
 public class Main {
 	static LessCompiler lessCompiler = new LessCompiler();
 
 	public static void compile(final String inputFilename, final String outputFilename) throws IOException, LessException {
 		File inputFile = new File(inputFilename);
 		if (outputFilename == null) {
 			String output = lessCompiler.compile(inputFile);
 			System.out.print(output);
 		}
 		else {
 			File outputFile = new File(outputFilename);
 			lessCompiler.compile(inputFile, outputFile);
 		}
 	}
 
 	public static void main(String[] args) {
 		Arguments arguments = Arguments.parse(args);
 		try {
 			skipAnnoyingEnvjsOutput();
 			compile(arguments.getInputFilename(), arguments.getOutputFilename());
 		} catch (Exception e) {
 			e.printStackTrace();
			throw new RuntimeException(e);
 		}
 	}
 
 	public static void skipAnnoyingEnvjsOutput() throws FileNotFoundException, LessException {
 		PrintStream originalSystemOut = new PrintStream(System.out);
 		System.setOut(new PrintStream(new ByteArrayOutputStream()));
 		lessCompiler.compile("");
 		System.setOut(originalSystemOut);
 	}
 }
