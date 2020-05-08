 package schedutron;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 
 
 public class FileManager {
 
 	ArrayList<Course> courseList = new ArrayList<Course>();
 
 	
 	public static BufferedReader getBufferedReader(File path) throws FileNotFoundException{

 		// Open the file that is the first 
 		// command line parameter
 		FileInputStream fstream = new FileInputStream(path);
 		// Get the object of DataInputStream
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
 		return br;
 	}
 	 
 	public void test(){
 		
 	}
 	
 	public File[] getFiles(String path){
 		class OnlyExt implements FilenameFilter {
 			String ext;
 			public OnlyExt(String ext) {
 			this.ext = "." + ext;
 			}
 			public boolean accept(File dir, String name) {
 			return name.endsWith(ext);
 			}
 		}
 		File folder = new File(path);
 		FilenameFilter only = new OnlyExt("txt"); 
 		File[] listOfFiles = folder.listFiles(only);
 		return listOfFiles;
 	}
 
 	public ArrayList<Course> makeClasses() throws IOException{
 		File[] files = getFiles("data");
 		String l;
 		for (int i = 0; i < files.length; i++) {
 			BufferedReader br = getBufferedReader(files[i]);
 			String strLine;
 			while ((strLine = br.readLine()) != null)   {
 				ArrayList<String> line_elements = new ArrayList<String>();
 
 				String[] line = strLine.split("\t");
 				if (line.length == 13 ){
 					ArrayList<String> els = new ArrayList<String>();
 
 					for(int a = 0; a < line.length; a++){
 						line[a] = line[a].trim();
 					//	if (line[a].equals("")){
 					//		continue;
 					//	}
 						els.add(line[a]);
 					}
 					Course course = new Course(els);
 					courseList.add(course);
 				} else {
 					System.out.println("not 13 long");
 					for(String el : line){
 						System.out.println(el);
 					}
 				}
 			}
 			
 		}
 		return courseList;
 	}
 }
 	
 
 
