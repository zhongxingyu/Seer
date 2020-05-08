 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 /**
  * File I/O in Java.
 * In Java, have buffers linked together in system.
 * Forwarding requests between input/output buffers(?)
 *
 * - Much more verbose than C and Python versions.
 * - Forces you to handle exceptions.
  */
 public class Files
 {
 	public static void main(String[] args)
 		throws FileNotFoundException, IOException
 	{
 		BufferedReader fp =
 			new BufferedReader(
 			new FileReader("sample.txt"));
 		
 		String line = fp.readLine();
 		while (line != null)
 		{
 			System.out.println(line);
 			line = fp.readLine();
 		}
		
		fp.close();
 	}
 }
