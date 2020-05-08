 package framework;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class SavedData {
 	boolean createDir,createFile;
 	
 	File savedDir, savedFile;
 	public String filePath="";
 	
 	public static void main(String[] args) throws IOException {
 		SavedData config = new SavedData("Config", "Settings");
 		SavedData saves = new SavedData("Save", "Saves");
 	}
 	
 	public SavedData(String dir, String baseFile) throws IOException {
 		
 		savedDir = new File(dir);
 		savedFile = new File(savedDir, (baseFile+".txt"));
 		
 		// if the directory doesn't exist, create it.
 		System.out.println("Checking directory: " + savedDir);
 		if (!savedDir.exists()) {
 			System.out.println("Creating directory: " + savedDir);
 			boolean result = savedDir.mkdir();  
 			
 			if(result) {    
 				System.out.println("Directory created.");
 			} else {
 				System.out.println("Directory could not be created.");
 			}
 		} else {
 			System.out.println("Directory already exists.");
 		}
 		
 		// if the file doesn't exist, create it.
 		System.out.println("Checking file: " + savedFile);
 		if (!savedFile.exists()) {
 			System.out.println("Creating file: " + savedFile);
 			boolean result = savedFile.createNewFile();  
 			
 			if(result) {    
 				System.out.println("File created.");
 				
 			} else {
 				System.out.println("File could not be created.");
 			}
 		} else {
 			System.out.println("File already exists.");
 			this.load(savedFile); //Load stuff from the file
 		}	
 
 		filePath = savedFile.getAbsoluteFile().toString();
 		System.out.println("File located at: " + filePath + "\n");
 		
 	}
 	
 	public void load(File savedFile) throws IOException {
 		String dir = savedFile.getAbsoluteFile().toString();
 		ArrayList<String> lines = new ArrayList<String>();
 		lines = FileWizard.readFile(dir);
		System.out.println(lines);
 	}
 	
 	public void save(ArrayList<String> lines) {
 		
 	}
 
 }
