 package rewirteFile;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class RewritePoi {
 
 	/**
 	 * @param args
 	 */
	
	private static String clusterFilePath = "C:\\Users\\WANG Haozhou\\Documents\\myUQ\\expData\\Dasfaa\\";
	
 	public static void main(String[] args) {
 		
 		
 		double maxX = 0;
 		double maxY = 0;
 		double minX = 180;
 		double minY = 180;
 		ArrayList<String> lines = new ArrayList<String>();
		//String clusterFilePath = "C:\\myUQ\\expData\\Dasfaa\\";
 		BufferedReader reader = null;
 		File file = new File("E:\\data.csv");
 		String lineWord;
 		try {
 			reader = new BufferedReader(new FileReader(file));
 			while ((lineWord = reader.readLine()) != null){
 				 String[] tr = lineWord.split(",");
 				 double tempX = Double.parseDouble(tr[0]);
 				 double tempY = Double.parseDouble(tr[1]);
 				 
 				 if(tempX < minX){
 					 minX = tempX;
 				 }
 				 
 				 if (tempX > maxX){
 					 maxX = tempX;
 				 }
 				 
 				 if(tempY < minY){
 					 minY = tempY;
 				 }
 				 
 				 if (tempY > maxY){
 					 maxY = tempY;
 				 }
 				 lines.add(lineWord);
 				 
 			}
 		} catch (NumberFormatException | IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		FileWriter outfile;
 		try {
 			outfile = new FileWriter(clusterFilePath + "points");
 			BufferedWriter bw = new BufferedWriter(outfile);
 			for (int i = 0; i < lines.size(); i = i + 10){
 				bw.write(i + "," + lines.get(i));
 	        	bw.newLine();
 			}
 			
 			bw.flush();
 			bw.close();
 			outfile.close();
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		System.out.println("max X is " + maxX );
 		System.out.println("max Y is " + maxY );
 		System.out.println("min X is " + minX );
 		System.out.println("min Y is " + minY );
 			
 			
 		
 		
 		
 		
 		// TODO Auto-generated method stub
 
 	}
 
 }
