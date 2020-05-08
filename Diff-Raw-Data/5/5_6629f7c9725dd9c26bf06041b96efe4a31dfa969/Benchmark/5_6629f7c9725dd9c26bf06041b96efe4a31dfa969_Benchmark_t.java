 package uk.ac.ucl.cs.clonedetector.benchmarking;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 
import uk.ac.ucl.cs.clonedetector.Index;
 
 public class Benchmark {
 
 	public static void main(String[] args) {
 
 		try {
 			long t1 = System.currentTimeMillis();
 			BufferedReader in = new BufferedReader(new FileReader("text/war&peace.txt"));
 			String line;
 
 			while ((line = in.readLine()) != null) {
				Index.computeFingerprint(line, args[0]);
 			}
 			long t2 = System.currentTimeMillis();
 			System.out.println(t2 - t1);
 		} catch (FileNotFoundException e) {
 			System.out.println("File not found!");
 		} catch (IOException e) {
 			System.out.println("An error occurred whilst reading the file.");
 		} catch (NoSuchAlgorithmException e) {
 			System.out.println("No such algorithm available on this system!");
 		}
 
 	}
 
 }
