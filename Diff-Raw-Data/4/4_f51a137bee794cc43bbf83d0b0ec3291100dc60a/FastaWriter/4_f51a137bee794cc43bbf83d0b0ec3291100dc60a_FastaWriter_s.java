 package fastaIO;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 public class FastaWriter {
 
 	public void writeFile(String output, String sequence, String description) {
 		PrintWriter pw = null;
 		try {
 			pw = new PrintWriter(output);
 		} catch (IOException e) {
 			System.out.println("An error ocurred while writing the file");
 		}  
		pw.write(">" + description);
		pw.write(sequence);
 		pw.close();
 	}
 	
 }
