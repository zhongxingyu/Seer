 package src;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 
 public class FilePath {
 	ArrayList<String[]> dataPath = new ArrayList<String[]>();
 	
 	public void readFile(){
 		BufferedReader br = null;
 		String[] pathOfFile;
 		try{
 			br = new BufferedReader(new FileReader("attribute.ini"));
 			while ((pathOfFile = br.readLine().split(" ==> ")) != null) {
 				dataPath.add(pathOfFile);
 			}
		}catch (Exception e) {	}
 	}
 	
 	public String[] getPath(int position){
 		return dataPath.get(position);
 	}
 	
 	public int getSize(){
 		return dataPath.size();
 	}
 }
