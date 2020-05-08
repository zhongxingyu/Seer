 import java.io.FileReader;
 import java.io.IOException;
 
 /**
  * 
  * @author Tobias Korfmacher
  *
  */
 public class MapLoader {
 	public static boolean twoPlayerSet;
	static int n = 13;
 	/**
 	 * 
 	 * @param i legt das zu ladene Level fest
 	 * @return map-Objekt
 	 */
 	public static int[][] laden(int i) {
 		twoPlayerSet = Menue.getMultiplayer();
 		
 		int c = 0;		
 		
 		int k = 0, l = 0;
 		int[][] map = new int[n][n];
 		String filename = "src/Maps/Level-" + i + ".txt";
 		
 		try {			
 			FileReader f = new FileReader(filename);
 			
 			System.out.println("Spielfeld eingelesen:"); // Test
 			
 			
 			while ((c = f.read()) != -1) {
 				if (Character.getNumericValue(c) != -1) {
 					map[k][l] = Character.getNumericValue(c);
 					
 					System.out.print(map[k][l] + ", ");	// Test
 					
 					if (k < n-1) {
 						k++;
 					}
 					
 					else if (l < n-1) {
 						System.out.println();	// Test
 						k = 0;
 						l++;
 					}
 					
 				}
 				
 			}
 			
 			System.out.println(); // Test
 			
 			if (twoPlayerSet){
 				map[n-2][n-2] = 10;
 			}
 			
 			System.out.println();	// Test
 			
 			f.close();
 
 		} 
 		
 		catch (IOException e) {
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 			System.exit(1);
 		}
 		
 		return map;
 
 	}
 	
 	// get_n-Methode:
 	public static int get_n() {
 		return n;
 	}
 }
