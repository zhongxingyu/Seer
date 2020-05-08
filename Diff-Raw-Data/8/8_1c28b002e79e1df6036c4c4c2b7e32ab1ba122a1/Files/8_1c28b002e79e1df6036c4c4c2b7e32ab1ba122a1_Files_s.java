 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 /**
  * File I/O in Java.
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
 	}
 }
