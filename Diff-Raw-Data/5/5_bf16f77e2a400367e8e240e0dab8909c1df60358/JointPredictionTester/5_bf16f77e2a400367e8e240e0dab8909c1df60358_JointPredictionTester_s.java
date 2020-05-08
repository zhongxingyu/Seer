 package edu.umd.cs.linqs.embers;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 public class JointPredictionTester {
 
 	public static void main(String [] args) {
		if (args.length < 3) 
 			throw new IllegalArgumentException("Usage: JointPredictionTester <input_json_file> <output_json_file> <optional_model>");
 		
 		try {
 			Scanner scanner = new Scanner(new File(args[0]));
 			FileWriter fw;
 			fw = new FileWriter(new File(args[1]));
			PSLJointRewriter rewriter = new PSLJointRewriter(args[2]);
 			
 			while (scanner.hasNext()) {
 				String line = scanner.nextLine();
 
 				String result = rewriter.process(line);
 
 				fw.write(result + "\n");
 
 			}
 			
 			fw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 }
