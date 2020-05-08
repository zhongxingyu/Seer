 package backend;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 /*
  * Stats Parser
  */
 public class StatParser {
 	
 	public static String[][] getStats(String file){
 		ArrayList<String[]> statsAL = new ArrayList<String[]>();		
 		try {
 			// Open the file that is the first 
 			FileInputStream fstream = new FileInputStream(file);
 			// Get the object of DataInputStream
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String str;
 			//Read File Line By Line
 			while((str = br.readLine()) != null){
 				statsAL.add(str.split(","));
 			}
 			in.close();
 			String[][] stats = new String[statsAL.size()][3];
 			for(int i=0; i<stats.length; i++){
				for(int j=0; j<3; j++)
 					stats[i][j] = statsAL.get(i)[j];
 			}
 			return stats;
 		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
 			return null;
 		} catch (IOException e) {
			System.out.println("Error reading file!");
 			return null;
 		} catch (IndexOutOfBoundsException e){
			System.out.println("Invalid stat file!");
 			return null;
 		}
 	}
 }
