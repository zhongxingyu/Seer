 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.FileFilter;
 import java.util.*;
 
 //Oleh: it is usually better to name classes as nouns. In this case something like "FileFinder" suits best.
 //Igor: Done
 public class FileFinder {
 	//Initialization collection with found files 
 	//class to filter required files
 
 //Oleh: maybe better to name filenameFilter
 //Igor: Done
 	private FilenameFilter filenameFilter;
 
 //Oleh: "isFolder is a good name for the boolean flag. in this case it would be better to name it folderFilter
 //Igor: Done
 	private FileFilter folderFilter;
 	//Initialization collection with list of searching files
 
 //Oleh: We will discuss generics soon. This is ok for now.
 	//private TreeSet foundFiles = new TreeSet();
 	private HashMap foundFiles = new HashMap();
 	public TreeSet searchingFiles = new TreeSet();
 	
 
 //Oleh: "ignore folders" is imperative voice. "IGNORED_FOLDERS" is a noun which is better.
 //Igor: Done
 	private final static TreeSet IGNORED_FOLDERS = new TreeSet();
 
 //Oleh: static initializer. good.
     static {
     	IGNORED_FOLDERS.add("System Volume Information");
     	IGNORED_FOLDERS.add(".");
     	IGNORED_FOLDERS.add("..");
 	}
 
 	public void findFiles (File rootFolder)	{
 //Oleh: camel case searchFolder (or maybe roorFolder?)
 //Igor: Done
 		
 		
 		filenameFilter = new FileFilterer(searchingFiles);
 		folderFilter = new FolderFilter(IGNORED_FOLDERS);
 		File[] fileList = rootFolder.listFiles(filenameFilter);
		File[] folderList = rootFolder.listFiles(folderFilter);
 		TreeSet fileSet = new TreeSet();
 //Oleh: for (File file : filelList) {
 //Igor: Fixed
 		//for (File file : fileList) {
 		fileSet.addAll(Arrays.asList(fileList));
 		foundFiles.put(rootFolder, fileSet);
 		//}
 
 //Oleh: for (File directory : folderList) {
 //Igor: Fixed				
 		for (File directory : folderList) {
 			findFiles(directory);
 					
 		}
 		
 	}
 		public void printResults ()	{
 		TreeSet filesSet = new TreeSet();
 		Set fileKeys = foundFiles.entrySet();
 		Iterator iter =  fileKeys.iterator();
 		while (iter.hasNext())	{
 			Map.Entry filemapEntry = (Map.Entry)iter.next();
 			System.out.println("In folder \""+filemapEntry.getKey()+ "\" found file(s):");
 			filesSet=(TreeSet)filemapEntry.getValue();
 			//System.out.println(filemapEntry.getValue());
 			for(Object fileObj :filesSet){
				File file = (File)fileObj;
 				System.out.println(file.getName());
 				
 			}
 					
 		}
 	  }
 		/* Implementation of filter class. This class is used to filter required files
 		 * in current folder*/
 class FileFilterer implements FilenameFilter{
 	private TreeSet fileNames;
 	public FileFilterer(TreeSet fileNames){
 		this.fileNames = fileNames;
 		}
 	public boolean accept(File directory, String filename) {
 			 return fileNames.contains(filename);
 			 }
 		}
 class FolderFilter implements FileFilter{
 	private TreeSet fileNames;
 	public FolderFilter(TreeSet fileNames){
 				this.fileNames = fileNames;
 		}
 	
 	public boolean accept(File fileName) {
 		if (fileName.isDirectory())	{
 			return !fileNames.contains(fileName.getName());
 		}else { 
 				return false;
 			   }
 				
 	}
 }		
 }
